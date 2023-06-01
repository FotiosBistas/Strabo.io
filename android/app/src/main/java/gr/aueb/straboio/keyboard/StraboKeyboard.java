package gr.aueb.straboio.keyboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pytorch.Module;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import gr.aueb.straboio.R;
import gr.aueb.straboio.keyboard.storage.CollectedDataManager;
import gr.aueb.straboio.keyboard.support.Sentence;
import gr.aueb.straboio.keyboard.support.Word;
import gr.aueb.straboio.model.LanguageModel;
import gr.aueb.straboio.model.LanguageModelLSTM;
import gr.aueb.straboio.model.Model;
import gr.aueb.straboio.model.TexVectorizer;

public class StraboKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kview;
    private Keyboard keyboard;
    private InputConnection iconn = null;

    private boolean isCaps = false;
    private boolean isEnglish = true;
    private boolean isSymbols = false;
    private CustomKeyMapper keymapper = new CustomKeyMapper();

    private Handler mHandler = new Handler();
    private int targetcode;
    private boolean isLongPressed = false;
    private boolean wasLongPressed = false;
    private static final int LONG_PRESS_TIMEOUT = 600; // in milliseconds

    private LanguageModel translator = null;
    // Translation Model stuff
    private Module model;
    private TexVectorizer texVectorizer;
    private Model lstmodel;
    private StringBuilder transInput = new StringBuilder("");
    private Sentence sentence;

    private boolean aiIsON = true;

    private final int BATCH_SIZE_LIMIT = 5;

    private Runnable mLongPressed = new Runnable() {
        public void run() {
            isLongPressed = true;
            // handle long press
            InputConnection iconn = getCurrentInputConnection();
            if (!isEnglish) {
                iconn.commitText(((Character) keymapper.toSPECIAL(targetcode)).toString(), 1);
                // Cache special character to buffer
                sentence.insertChar(
                        ((Character) keymapper.toSPECIAL(targetcode)).toString(),
                        getActualCursorPosition()
                );
                wasLongPressed = !wasLongPressed;

            }
            isLongPressed = false;
        }
    };

    // Handle inter-service messaging:
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle NOTIFY_VIEW_TEXT_SELECTION_CHANGED:
            ExtractedText et = getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0);
            String potentialEmptyText = (et!=null) ? et.text.toString() : "";
            if(potentialEmptyText.equals("")){
                // Check if sentence is not empty:
                if(!sentence.isEmpty()){
                    CollectedDataManager.getInstance().add(getApplicationContext(), sentence.toJSON());
                    new CollectTask().execute(sentence.toJSON()); // Use copy constructor to stop next line from erasing the sentence.
                }
                sentence.erase();
                transInput = new StringBuilder();
            }
        }
    };

    // Handle inter-service messaging:
    private BroadcastReceiver modelChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle NOTIFY_VIEW_TEXT_SELECTION_CHANGED:
            try {
                System.out.println("Changing model...");
                loadModel();
                System.out.println("Model changed successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("ERR_LOAD_MODULE", "onCreate: " + e.getMessage());
            }
        }
    };

    public static String getAssetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }
        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    private void loadModel() throws IOException {

        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.APP_SHARED_PREFS), Context.MODE_PRIVATE);
        final String CURRENT_MODEL = sharedPreferences.getString(getResources().getString(R.string.MODEL_CURRENT_NAME_SHARED_PREF), "none.pt");

        try{
            File dir = new File(
                    getFilesDir(),
                    getResources().getString(R.string.MODEL_DIRECTORY)
            );

            File currModel = new File(dir, CURRENT_MODEL);

            model = Module.load(
                    currModel.getAbsolutePath()
            );
            System.out.println("LATEST MODEL LOADED.");
        } catch(Exception e){
            // The user has never pulled a new model from the server. Load default:
            model = Module.load(
                    getAssetFilePath(this, "SCR_OPT_LSTM_LM_50000_char_120_32_512.pt")
            );
        }

        texVectorizer = new TexVectorizer(
                TexVectorizer.Mode.CHAR,
                getAssetFilePath(this, "vocab_50000_char_120_128_1024.csv")
        );
        lstmodel = new Model(model, 120, 32, 512, 120);
        translator = new LanguageModelLSTM(lstmodel, texVectorizer, 3);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Setup model.
        try {
            loadModel();
        } catch (IOException e) {
            Log.d("ERR_LOAD_MODULE", "onCreate: " + e.getMessage());
        }

        // Setup message receiver:
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver,
                new IntentFilter(
                        getResources().getString(R.string.ACTION_VIEW_TEXT_SELECTION_CHANGED)
                )
        );

        // Setup message receiver:
        LocalBroadcastManager.getInstance(this).registerReceiver(
                modelChangedReceiver,
                new IntentFilter(
                        getResources().getString(R.string.ACTION_LATEST_MODEL_CHANGED)
                )
        );

        // Setup training data collector buffer.
        sentence = new Sentence();
    }

    @Override
    public View onCreateInputView() {
        kview = (KeyboardView) getLayoutInflater().inflate(R.layout.strabo_keyboard_layout, null);
        keyboard = new Keyboard(this, R.xml.strabo_qwerty);
        kview.setKeyboard(keyboard);
        kview.setOnKeyboardActionListener(this);
        return kview;
    }

    @Override
    public void onPress(int i) {
        // Disables pop-up preview for special keys (space, shift, backspace)
        kview.setPreviewEnabled((i != 32) && (i != Keyboard.KEYCODE_SHIFT) && (i != Keyboard.KEYCODE_DELETE));
        // Special character on long press
        isLongPressed = false;
        targetcode = i;
        mHandler.postDelayed(mLongPressed, LONG_PRESS_TIMEOUT);

    }

    @Override
    public void onRelease(int i) {
        mHandler.removeCallbacks(mLongPressed);
        if (!isLongPressed) {
            // handle short press
        }
    }

    @Override
    public void onKey(int i, int[] ints) {
        iconn = getCurrentInputConnection();
        playSoundEffect(i);
        switch (i) {
            case Keyboard.KEYCODE_DELETE:
                iconn.deleteSurroundingText(1, 0);
                // Delete character from buffer:
                sentence.delete(
                        getActualCursorPosition()
                );
                if (aiIsON)
                    if (transInput.length() > 0)
                        transInput.deleteCharAt(transInput.length() - 1);
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kview.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                iconn.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case -999:
                isEnglish = !isEnglish;
                isSymbols = false;
                for(Keyboard.Key k: keyboard.getKeys())
                    if(k.codes[0] == -888)
                        k.label = ";!?";
                updateAlphabetOfKeyboard();
                break;
            case -888:
                isSymbols = !isSymbols;
                switchToSymbols();
                break;
            default:
                String characterToOutput;
                char code = (char) i;;
                if(!isSymbols){
                    if(Character.isLetter(code) && isCaps)
                        code = Character.toUpperCase(code);

                    characterToOutput = String.valueOf((isEnglish) ? keymapper.toEN(code) : keymapper.toGR(code));
                } else {
                    characterToOutput = String.valueOf(keymapper.toSYMBOL(i));
                }

                iconn.commitText(characterToOutput, 1);
                if (wasLongPressed) {
                    iconn.deleteSurroundingText(1, 0);
                }


                /*
                    We only want the translation to take place if:
                        - translation is enabled in the first place
                        - user is typing with latin characters
                 */
                if (aiIsON && isEnglish) {
                    transInput.append(characterToOutput);
                    // Translate:
                    if (code == 32 && isEnglish) { // SPACE was pressed and is english
                        if(!transInput.toString().equals(" "))
                            new CorrectTask().execute(iconn.getExtractedText(new ExtractedTextRequest(), 0).text.subSequence(0,getActualCursorPosition()).toString(), new StringBuilder(transInput).toString());
                        transInput = new StringBuilder("");
                    } else if (code == 32) {
                        transInput = new StringBuilder("");
                    }
                } else {
                    if(!wasLongPressed){
                        sentence.insertChar(
                                characterToOutput,
                                getActualCursorPosition()
                        );
                    }
                }
                // Reset long press detection
                wasLongPressed = false;
        }
    }

    private int getActualCursorPosition(){
        InputConnection inputConnection = getCurrentInputConnection();
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        int currentCursorPosition = extractedText.startOffset + extractedText.selectionStart;
        return currentCursorPosition;
    }

    private void updateAlphabetOfKeyboard() {
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (isEnglish) {
                // Update key labels for English language
                // For example, you can set the label to "A" for the letter "A"
                if (key.codes[0] == -999) {
                    key.label = "en";
                }
                if (key.label != null && keymapper.isSwitchable(key.codes[0])) {
                    key.label = ((Character) keymapper.toEN(key.codes[0])).toString();
                }
            } else {
                // Update key labels for other language
                if (key.codes[0] == -999) {
                    key.label = "ελ";
                }
                if (key.label != null && keymapper.isSwitchable(key.codes[0])) {
                    key.label = ((Character) keymapper.toGR(key.codes[0])).toString();
                }
            }
        }
        // Redraw the keyboard to update the key labels
        kview.invalidateAllKeys();

    }

    private void switchToSymbols() {
        if(isSymbols){
            for(Keyboard.Key key : keyboard.getKeys()){
                if(key.codes[0] == -888){
                    key.label = "ab";
                }
                if (key.label != null && keymapper.isSwitchable(key.codes[0])) {
                    key.label = ((Character) keymapper.toSYMBOL(key.codes[0])).toString();
                }
            }
            // Redraw the keyboard to update the key labels
            kview.invalidateAllKeys();
        } else {
            for(Keyboard.Key key : keyboard.getKeys())
                if(key.codes[0] == -888)
                    key.label = ";!?";
            // Switch back to the alphabet
            updateAlphabetOfKeyboard();
        }
    }

    private void playSoundEffect(int i) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (i) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {
        aiIsON = !aiIsON;
        if (!aiIsON)
            transInput = new StringBuilder("");
        Toast.makeText(getApplicationContext(), "AI assist " + (aiIsON ? "ON" : "OFF") + ".", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void swipeUp() {

    }

    public static String getServiceName(){
        return "gr.aueb.straboio.keyboard.StraboKeyboard";
    }

    private class CorrectTask extends AsyncTask<String, Void, Void> {

        private String transOutput = "";
        private String targetTransInput = "";
        private String textUpUntilThatPoint;

        @Override
        protected Void doInBackground(String... strings) {
            textUpUntilThatPoint = strings[0];
            targetTransInput = strings[1];
            transOutput = translator.translate(targetTransInput); // translate
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            int end = Math.max(textUpUntilThatPoint.length() - targetTransInput.length(), 0);
            iconn.setComposingRegion(textUpUntilThatPoint.substring(0, end).length(), textUpUntilThatPoint.length());
            // Get the current cursor position
            int relativePosition = textUpUntilThatPoint.substring(0, end).length() + transOutput.length();
            iconn.setComposingText(transOutput,
                    /*
                        According to the Android docs;
                        "
                            setComposingText's second argument (newCursorPosition) is the new cursor position around the text.
                            If > 0, this is relative to the end of the text - 1; if <= 0, this is relative to the start of the text.
                            So a value of 1 will always advance you to the position after the full text being inserted.
                            Note that this means you can't position the cursor within the text, because the editor can make modifications
                            to the text you are providing so it is not possible to correctly specify locations there.
                        "

                        So there are two possibilities:
                        1) A word needs to be translated but we have already proceeded to typing another word.
                                (Thus: actualCursorPosition > relativePosition -> relativePosition)
                        2) The word that needs to be translated was just typed and we have not moved on to the next word.
                                (Thus: actualCursorPosition == relativePosition -> 1)

                        This *must* be done because the translation happens asynchronously.
                     */
                    (getActualCursorPosition() > relativePosition) ? relativePosition : 1

            );
            iconn.finishComposingText();
            // Update buffer with translated output.
            targetTransInput = targetTransInput.replace(" ", "");
            transOutput = transOutput.replace(" ", "");

            Word newWord = new Word(
                    new StringBuilder(targetTransInput),
                    new StringBuilder(transOutput),
                    textUpUntilThatPoint.substring(0, end).length() + 1,
                    textUpUntilThatPoint.substring(0, end).length() + transOutput.length()
            );

            sentence.insertWord(newWord);
        }
    }

    /**
     * Collects a training data sentence and stores it asynchronously.
     */
    private class CollectTask extends AsyncTask<JSONObject, Void, Void>{

        private int size;

        @Override
        protected Void doInBackground(JSONObject... jsonObjects) {
            JSONObject target = jsonObjects[0];
            size = CollectedDataManager.getInstance().add(
                    getApplicationContext(),
                    target
            );
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if(size >= BATCH_SIZE_LIMIT){
                new PushTask().execute();
            }
        }
    }

    /**
     * Pushes the batch to the servers.
     */
    private class PushTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                // Create the JSON payload
                JSONArray batch = CollectedDataManager.getInstance().retrieveData(getApplicationContext());
                // Flush collected data...
                CollectedDataManager.getInstance().flush(getApplicationContext());

                // Prepare payload
                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("batch", batch);

                // SSL STUFF:
                // Load CAs from an InputStream
                // (could be from a resource or ByteArrayInputStream or ...)
                CertificateFactory cf = CertificateFactory.getInstance("X.509");

                // My CRT file that I put in the assets folder
                // I got this file by following these steps:
                // * Go to https://littlesvr.ca using Firefox
                // * Click the padlock/More/Security/View Certificate/Details/Export
                // * Saved the file as littlesvr.crt (type X.509 Certificate (PEM));
                InputStream caInput = new BufferedInputStream(getAssets().open("cert.der"));
                Certificate ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                // Create an SSLContext that uses our TrustManager
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);

                // URL of the API endpoint
                URL url = new URL(
                        "https://"+
                                getResources().getString(R.string.SERVER_ADDR)+
                                ":"+
                                getResources().getString(R.string.SERVER_PORT)+
                                "/Batch"
                );

                // Create an HTTPS connection
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setSSLSocketFactory(sslContext.getSocketFactory());

                // Set request method to POST
                connection.setRequestMethod("POST");

                // Set content type and request property
                connection.setRequestProperty("Content-Type", "application/json");

                // Enable output and disable caching
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                // Write the JSON payload to the output stream
                OutputStreamWriter outputStream = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                System.out.println(jsonPayload);
                outputStream.write(jsonPayload.toString());
                outputStream.flush();
                outputStream.close();

                // Send the request and retrieve the response code
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response from the input stream
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();

                    // Process the response
                    String responseData = response.toString();
                    // ... do something with the response data
                    System.out.println(responseData);
                } else {
                    // Handle error response
                    System.err.println("Response error: (Code "+responseCode+")");

                }

                // Close the connection
                connection.disconnect();

            } catch (IOException | JSONException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}