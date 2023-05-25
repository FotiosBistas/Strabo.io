package gr.aueb.straboio.keyboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import org.json.JSONObject;
import org.pytorch.Module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    private CustomKeyMapper keymapper = new CustomKeyMapper();

    private Handler mHandler = new Handler();
    private int targetcode;
    private boolean isLongPressed = false;
    private boolean wasLongPressed = false;
    private static final int LONG_PRESS_TIMEOUT = 600; // in milliseconds

    private LanguageModel translator = null;
    private StringBuilder transInput = new StringBuilder("");
    private Sentence sentence;

    private boolean aiIsON = true;

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

    @Override
    public void onCreate() {
        super.onCreate();
        // Setup model.
        try {
            Module model = Module.load(getAssetFilePath(this, "SCR_OPT_LSTM_LM_50000_char_120_32_512.pt"));
            TexVectorizer texVectorizer = new TexVectorizer(
                    TexVectorizer.Mode.CHAR,
                    getAssetFilePath(this, "vocab_50000_char_120_128_1024.csv")
            );
            Model lstmodel = new Model(model, 120, 32, 512, 120);
            translator = new LanguageModelLSTM(lstmodel, texVectorizer, 3);
        } catch (IOException e) {
            Log.d("ERR_LOAD_MODULE", "onCreate: " + e.getMessage());
        }

        // Setup message receiver:
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver,
                new IntentFilter("gr.aueb.straboio.NOTIFY_VIEW_TEXT_SELECTION_CHANGED")
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
                updateAlphabetOfKeyboard();
                break;
            default:
                char code = (char) i;
                if(Character.isLetter(code) && isCaps)
                    code = Character.toUpperCase(code);

                String characterToOutput = String.valueOf((isEnglish) ? keymapper.toEN(code) : keymapper.toGR(code));
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

    private String getOuputedText(){
        InputConnection inputConnection = getCurrentInputConnection();
        String ouputedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text.toString();
        return (ouputedText != null) ? ouputedText : "";
    }

    private void updateAlphabetOfKeyboard() {
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (isEnglish) {
                // Update key labels for English language
                // For example, you can set the label to "A" for the letter "A"
                if (key.codes[0] == -999) {
                    key.label = "en";
                }
                if (key.label != null && (Character.isLetter(key.codes[0]))) {
                    key.label = ((Character) keymapper.toEN(key.codes[0])).toString();
                }
            } else {
                // Update key labels for other language
                if (key.codes[0] == -999) {
                    key.label = "ελ";
                }
                if (key.label != null && Character.isLetter(key.codes[0])) {
                    key.label = ((Character) keymapper.toGR(key.codes[0])).toString();
                }
            }
        }
        // Redraw the keyboard to update the key labels
        kview.invalidateAllKeys();

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

        @Override
        protected Void doInBackground(JSONObject... jsonObjects) {
            JSONObject target = jsonObjects[0];
            CollectedDataManager.getInstance().add(
                    getApplicationContext(),
                    target
            );
            return null;
        }
    }

    private class PushTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            /*
                TODO: Send the collected training data pairs to the central server.
             */
            return null;
        }
    }
}