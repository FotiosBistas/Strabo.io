package gr.aueb.straboio;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gr.aueb.straboio.keyboard.CaptureTextService;

public class MainActivity extends AppCompatActivity {

    private Button trainingDataBtn;
    private Button checkForNewModelBtn;
    private TextView dataCollectionStatusTextView;
    private TextView dataCollectionHelpTextView;
    private TextView currentModelText;
    private TextView lastTimeCheckedText;

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
                // TODO: check for new model from the server
                saveLastTimeChecked();
                loadLastTimeCheked();
            }
        });
    }

    private void loadLastTimeCheked() {
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.APP_SHARED_PREFS), Context.MODE_PRIVATE);
        String lastCheckedDate = sharedPreferences.getString(getResources().getString(R.string.MODEL_LAST_CHECKED_SHARED_PREF), "???");
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
        updateDataCollectionStatusText();
        updateCurrentModelText();
        loadLastTimeCheked();
    }

    private void updateCurrentModelText(){
        currentModelText.setText(Html.fromHtml(CURRENT_MODEL_TEXT+"<b>"+"XXX.XXX"+"</b>"));
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
}