package gr.aueb.straboio.keyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import gr.aueb.straboio.R;

public class StraboKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kview;
    private Keyboard keyboard;

    private boolean isCaps = false;
    private boolean isEnglish = true;
    private CustomKeyMapper keymapper = new CustomKeyMapper();

    private Handler mHandler = new Handler();
    private int targetcode;
    private boolean isLongPressed = false;
    private boolean wasLongPressed = false;
    private static final int LONG_PRESS_TIMEOUT = 600; // in milliseconds

    private Runnable mLongPressed = new Runnable() {
        public void run() {
            isLongPressed = true;
            // handle long press
            Log.d("longpressdet", "run: pressing...");
            InputConnection iconn = getCurrentInputConnection();
            if(!isEnglish){
                iconn.commitText(((Character) keymapper.toSPECIAL(targetcode)).toString(), 1);
                wasLongPressed =! wasLongPressed;

            }
            isLongPressed = false;
        }
    };

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
        kview.setPreviewEnabled( (i != 32) && (i != Keyboard.KEYCODE_SHIFT) && (i != Keyboard.KEYCODE_DELETE));
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
            Log.d("longpressdet", "run: stopped!");
        }
    }

    @Override
    public void onKey(int i, int[] ints) {
        InputConnection iconn = getCurrentInputConnection();
        playSoundEffect(i);
        switch (i) {
            case Keyboard.KEYCODE_DELETE:
                iconn.deleteSurroundingText(1,0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps  = !isCaps;
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
                if(wasLongPressed){
                    iconn.deleteSurroundingText(1,0);
                    wasLongPressed = !wasLongPressed;
                }
        }
    }

    private void updateAlphabetOfKeyboard() {
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (isEnglish) {
                // Update key labels for English language
                // For example, you can set the label to "A" for the letter "A"
                if(key.codes[0] == -999){
                    key.label = "en";
                }
                if (key.label != null && (Character.isLetter(key.codes[0]))) {
                    key.label = ((Character) keymapper.toEN(key.codes[0])).toString();
                }
            } else {
                // Update key labels for other language
                if(key.codes[0] == -999){
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

    private void playSoundEffect(int i){
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch(i){
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

    }

    @Override
    public void swipeUp() {

    }
}