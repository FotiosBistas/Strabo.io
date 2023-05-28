package gr.aueb.straboio;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import gr.aueb.straboio.keyboard.CaptureTextService;
import gr.aueb.straboio.keyboard.StraboKeyboard;

public class MainActivity extends AppCompatActivity {

    private Button trainingDataBtn;
    private Button checkForNewModelBtn;
    private Button enableKeyboardBtn;
    private TextView dataCollectionStatusTextView;
    private TextView dataCollectionHelpTextView;
    private TextView currentModelText;
    private TextView lastTimeCheckedText;
    private TextView enableKeyboardText;

    private ProgressDialog downloadProgressDialog;

    private final String TRAINING_DATA_COLLECTION_BTN_TEXT = "Training Data Collection";
    private final String CURRENT_MODEL_TEXT = "Currently on model: ";
    private final String LAST_TIME_CHECKED_TEXT = "Last time checked: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trainingDataBtn = (Button) findViewById(R.id.trainingDataBtn);
        checkForNewModelBtn = (Button) findViewById(R.id.checkForNewModelBtn);
        dataCollectionStatusTextView = (TextView) findViewById(R.id.dataCollectionStatusTextView);
        dataCollectionHelpTextView = (TextView) findViewById(R.id.dataCollectionHelpTextView);
        currentModelText = (TextView) findViewById(R.id.currentModelText);
        lastTimeCheckedText = (TextView) findViewById(R.id.lastTimeCheckedText);
        enableKeyboardBtn = (Button) findViewById(R.id.enableKeyboardBtn);
        enableKeyboardText = (TextView) findViewById(R.id.enableKeyboardText);

        updateEnableSelectionStatus();
        updateDataCollectionStatusText();
        setDataCollectionHelpText();
        updateCurrentModelText();
        loadLastTimeCheked();

        trainingDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Enable text monitor accessibility, so that we can capture the outputed text.
                Intent settingsIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(settingsIntent);
            }
        });

        checkForNewModelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getApplicationContext(), "Checking for new model...", Toast.LENGTH_SHORT);
                toast.show();
                new PeekTask().execute();
                saveLastTimeChecked();
                loadLastTimeCheked();
            }
        });

        enableKeyboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                // Check if the custom keyboard is already enabled
                boolean isCustomKeyboardEnabled = false;
                for(InputMethodInfo im : imm.getEnabledInputMethodList()){
                    if(im.getServiceName().equals(StraboKeyboard.getServiceName()))
                        isCustomKeyboardEnabled = true;
                }

                if (isCustomKeyboardEnabled) {
                    // The custom keyboard is already enabled, show it
                    imm.showInputMethodPicker();
                } else {
                    // The custom keyboard is not enabled, prompt the user to enable it
                    Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                    startActivity(intent);
                }
            }
        });
    }

    private void updateEnableSelectionStatus() {
        if (isCustomKeyboardEnabled()) {
            // The custom keyboard is already enabled,
            enableKeyboardText.setText(Html.fromHtml(
                    "Strabo.io <b>ENABLED</b> as input source."
            ));
            enableKeyboardBtn.setText(Html.fromHtml("<b>Select</b> Strabo.io Keyboard"));
        } else {
            // The custom keyboard is not enabled, prompt the user to enable it
            enableKeyboardText.setText(Html.fromHtml(
                    "Strabo.io <b>NOT ENABLED</b> as input source."
            ));
            enableKeyboardBtn.setText(Html.fromHtml("<b>Enable</b> Strabo.io Keyboard"));
        }
    }

    private boolean isCustomKeyboardEnabled() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // Check if the custom keyboard is already enabled
        boolean isCustomKeyboardEnabled = false;
        for(InputMethodInfo im : imm.getEnabledInputMethodList()){
            if(im.getServiceName().equals(StraboKeyboard.getServiceName()))
                isCustomKeyboardEnabled = true;
        }
        return isCustomKeyboardEnabled;
    }

    private void loadLastTimeCheked() {
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.APP_SHARED_PREFS), Context.MODE_PRIVATE);
        String lastCheckedDate = sharedPreferences.getString(getResources().getString(R.string.MODEL_LAST_CHECKED_SHARED_PREF), "Never");
        lastTimeCheckedText.setText(Html.fromHtml(LAST_TIME_CHECKED_TEXT+"<b>"+lastCheckedDate+"</b>."));
    }

    private void saveLastTimeChecked() {
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.APP_SHARED_PREFS), Context.MODE_PRIVATE);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss, dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getResources().getString(R.string.MODEL_LAST_CHECKED_SHARED_PREF), currentDate);
        editor.apply();
    }

    private void setDataCollectionHelpText() {
        final String HELP_TEXT = "To <b>enable/disable</b> Training Data Collection:<br>1. Tap the button above.<br>2. You will be redirected to the Accessibility Settings. Under '<b>Downloaded Apps</b>', select '<b>Send Training Data</b>'.<br>3. Toggle '<b>Use Send Training Data</b>'.";
        dataCollectionHelpTextView.setText(Html.fromHtml(HELP_TEXT));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateEnableSelectionStatus();
        updateDataCollectionStatusText();
        updateCurrentModelText();
        loadLastTimeCheked();
    }

    private void updateCurrentModelText(){
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.APP_SHARED_PREFS), Context.MODE_PRIVATE);
        String currentModelName = sharedPreferences.getString(getResources().getString(R.string.MODEL_CURRENT_NAME_SHARED_PREF), "SCR_OPT_LSTM_LM_50000_char_120_32_512.pt");
        currentModelText.setText(Html.fromHtml(CURRENT_MODEL_TEXT+"<b>"+currentModelName+"</b>"));
    }

    private void updateDataCollectionStatusText(){
        SpannableString styledText;
        if(isAccessibilityServiceEnabled(getApplicationContext(), CaptureTextService.class)){
            String text = "Training data IS being collected.";
            styledText = new SpannableString(text);
            styledText.setSpan(new StyleSpan(Typeface.BOLD), 14, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            trainingDataBtn.setText("Disable " + TRAINING_DATA_COLLECTION_BTN_TEXT);
        } else {
            String text = "Training data is NOT being collected.";
            styledText = new SpannableString(text);
            styledText.setSpan(new StyleSpan(Typeface.BOLD), 17, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            trainingDataBtn.setText("Enable " + TRAINING_DATA_COLLECTION_BTN_TEXT);
        }
        dataCollectionStatusTextView.setText(styledText);
    }

    private boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager != null) {
            // Get all enabled Accessibility Services
            List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

            for (AccessibilityServiceInfo service : enabledServices) {
                // Check if the service matches Accessibility Service class
                if (service.getResolveInfo().serviceInfo.packageName.equals(context.getPackageName())
                        && service.getResolveInfo().serviceInfo.name.equals(serviceClass.getName())) {
                    return true; // Service is enabled
                }
            }
        }

        return false; // Service is not enabled
    }

    private class PeekTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder response = new StringBuilder();
            try {
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
                                "/peek"
                );

                // Create an HTTPS connection
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setSSLSocketFactory(sslContext.getSocketFactory());

                // Set request method to GET
                connection.setRequestMethod("GET");
                // Set timeout
                connection.setConnectTimeout(5000);

                // Send the request and retrieve the response code
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    connection.disconnect();
                } else {
                    // Handle error response
                    System.err.println("HTTP request failed with response code: " + responseCode);
                }

                // Close the connection
                connection.disconnect();

            } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }

            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                // Parse the JSON response
                JSONObject jsonObject = new JSONObject(result);
                String modelName = jsonObject.getString("model_name");
                System.out.println("Latest model name: " + modelName);
                if(!isTheSameModel(modelName)){
                    createDownloadPromptDialog(modelName);
                } else {
                    createAlreadyOnCurrentModelDialog();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void createAlreadyOnCurrentModelDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Latest Model")
                    .setMessage("You are using the latest model.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }

        private boolean isTheSameModel(String modelName) {
            SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.APP_SHARED_PREFS), Context.MODE_PRIVATE);
            String currentModelName = sharedPreferences.getString(getResources().getString(R.string.MODEL_CURRENT_NAME_SHARED_PREF), "none.pt");
            return modelName.equals(currentModelName);
        }

        private void createDownloadPromptDialog(String nameOfModel) {
            // Create a Yes/No dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("New Model found")
                    .setMessage("Do you want to download the latest model: '" + nameOfModel + "'?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // Start the AsyncTask to download the file
                            new UpdateTask().execute(nameOfModel);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // User clicked "No"
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
    }

    /**
     * Pulls the latest model from the server.
     */
    private class UpdateTask extends AsyncTask<String, Integer, Boolean> {

        private String LATEST_MODEL_FILE_NAME;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create and show the progress dialog
            downloadProgressDialog = new ProgressDialog(MainActivity.this);
            downloadProgressDialog.setMessage("Downloading Model...");
            downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            downloadProgressDialog.setIndeterminate(false);
            downloadProgressDialog.setMax(100);
            downloadProgressDialog.setProgress(0);
            downloadProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            LATEST_MODEL_FILE_NAME = strings[0];
            int fileLength;
            int totalBytesRead = 0;
            try {
                // SSL STUFF:
                // Load CAs from an InputStream
                // (could be from a resource or ByteArrayInputStream or ...)
                CertificateFactory cf = CertificateFactory.getInstance("X.509");

                InputStream caInput = new BufferedInputStream(getAssets().open("cert.der"));
                Certificate ca = cf.generateCertificate(caInput);
                

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
                                "/Model"
                );

                // Create an HTTPS connection
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setSSLSocketFactory(sslContext.getSocketFactory());

                // Set request method to GET
                connection.setRequestMethod("GET");
                // Set timeout
                connection.setConnectTimeout(5000);

                // Send the request and retrieve the response code
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Get the file length
                    fileLength = connection.getContentLength();

                    InputStream inputStream = connection.getInputStream();

                    // Get the directory where models are saved:
                    File dir = new File(
                            getFilesDir(),
                            getResources().getString(R.string.MODEL_DIRECTORY)
                    );

                    // Check if the directory exists...
                    if(!dir.exists()){
                        // If not, create
                        dir.mkdir();
                    }

                    // Create a file in the internal storage
                    File file = new File(dir, LATEST_MODEL_FILE_NAME);

                    // Create a file output stream to write the response to the file
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    // Read the response and write it to the file
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                        // Update progress
                        totalBytesRead += bytesRead;
                        int progress = (int) ((totalBytesRead * 100) / fileLength);
                        publishProgress(progress);
                    }

                    // Close the streams
                    fileOutputStream.close();
                    inputStream.close();

                    System.out.println("File downloaded: " + file.getAbsolutePath());
                    // Close the connection
                    connection.disconnect();
                    return true;
                } else {
                    // Handle error response
                    System.err.println("Response error: (Code "+responseCode+")");
                }

                // Close the connection
                connection.disconnect();
                return false;
            } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Update the progress dialog
            downloadProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            downloadProgressDialog.dismiss();
            Toast resultToast;
            if (result) {
                // File downloaded successfully
                resultToast = Toast.makeText(getApplicationContext(), "Model downloaded successfully.", Toast.LENGTH_SHORT);

                final String CURRENT_MODEL = getCurrentModelName();

                File dir = new File(
                        getFilesDir(),
                        getResources().getString(R.string.MODEL_DIRECTORY)
                );

                // Create a file in the internal storage
                File currModel = new File(dir, CURRENT_MODEL);
                currModel.delete();


                Intent intentNotify = new Intent(
                        getResources().getString(R.string.ACTION_LATEST_MODEL_CHANGED)
                );
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intentNotify);
                updateCurrentModelName(LATEST_MODEL_FILE_NAME);
                updateCurrentModelText();
                System.out.println("New model downloaded.");

            } else {
                // Handle download failure
                resultToast = Toast.makeText(getApplicationContext(), "Model download faliled.", Toast.LENGTH_SHORT);
            }
            resultToast.show();
        }

        private String getCurrentModelName() {
            SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.APP_SHARED_PREFS), Context.MODE_PRIVATE);
            String currentModelName = sharedPreferences.getString(getResources().getString(R.string.MODEL_CURRENT_NAME_SHARED_PREF), "SCR_OPT_LSTM_LM_50000_char_120_32_512.pt");
            return currentModelName;
        }

        private void updateCurrentModelName(String latest_model_file_name) {
            SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.APP_SHARED_PREFS), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getResources().getString(R.string.MODEL_CURRENT_NAME_SHARED_PREF), latest_model_file_name);
            editor.apply();
        }
    }
}