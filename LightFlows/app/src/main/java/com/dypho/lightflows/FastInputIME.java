package com.dypho.lightflows;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FastInputIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private static InputConnection ic;
    private boolean windowShown = false;
    // private boolean isPressing = false;
    public static String base64Image;
    //public static String words = "null";
    public static boolean couldCatch = true;
    public static boolean isCapturing = false;
    public static boolean autoclear = false;
    public static boolean autoenter = false;
    private static boolean onreleased=false;

    public static boolean ifinput = false;
    public static Keyboard keyboard;
    public static KeyboardView keyboardView;
    public static int keycode;
    public static boolean ifleft;
    private Handler handler = new Handler();
    public static boolean isLongPressLeft = false;
    public static boolean isLongPressRight = false;
    public static boolean isLongPressDelete = false;
    private static ExecutorService executorService;
    private static boolean couldinputspace = true;
    private static Future<?> future;

    // private static File file;
    private Runnable moveCursorLeftTask =
            new Runnable() {
                @Override
                public void run() {
                    if (isLongPressLeft) {
                        // 实现向左移动光标的逻辑
                        // ...
                        ic.sendKeyEvent(
                                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                        ic.sendKeyEvent(
                                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
                        handler.postDelayed(this, 75); // 100毫秒后再次执行
                    }
                }
            };
    private Runnable moveCursorRightTask =
            new Runnable() {
                @Override
                public void run() {
                    if (isLongPressRight) {
                        // 实现向左移动光标的逻辑
                        // ...
                        ic.sendKeyEvent(
                                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                        ic.sendKeyEvent(
                                new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                        handler.postDelayed(this, 75); // 100毫秒后再次执行
                    }
                }
            };
    private Runnable moveCursorDeleteTask =
            new Runnable() {
                @Override
                public void run() {
                    if (isLongPressDelete) {
                        // 实现向左移动光标的逻辑
                        // ...
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                        handler.postDelayed(this, 75); // 100毫秒后再次执行
                    }
                }
            };

    @Override
    public View onCreateInputView() {
        keyboardView = new KeyboardView(getApplicationContext(), null);

        // 在此处导入键盘布局
        keyboard = new Keyboard(getApplicationContext(), R.layout.keyboard);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        Catcher.init();
        turnon();
        //File file = new File(getExternalFilesDir("temp"), "tmp.jpg");
        //Catcher.filePath = file.getAbsolutePath();
        //Catcher.filePath = "/sdcard/Pictures/tmp.jpg";
        loadSettings();
        SpeechApp.initializeMsc(FastInputIME.this);
        IatDemo.init(getApplicationContext());
        return keyboardView;
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        /** 进入此回调时，用户将开始输入 在此时保存InputConnection，用于后续的输入操作 * */
        ic = getCurrentInputConnection();
        Catcher.initCameara();
        turnon();

        windowShown = true;
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();
        /** 进入此回调时，用户已结束输入 在此时清理InputConnection * */
        Catcher.stopCatch();
        Catcher.closeCamera();
        ic = null;
        windowShown = false;
        isLongPressLeft = false;
        isLongPressRight = false;
        isLongPressDelete = false;
        stopLongPressLeft();
        stopLongPressRight();
        stopLongPressDelete();
    }

    @Override
    public void onPress(int primaryCode) {
        switch (primaryCode) {
            case 1: // 左移光标
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
                if (isLongPressLeft != true) {

                    isLongPressLeft = true;
                    handler.postDelayed(moveCursorLeftTask, 500);
                }
                break;
            case 2: // 右移光标
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                if (isLongPressRight != true) {

                    isLongPressRight = true;
                    handler.postDelayed(moveCursorRightTask, 500);
                }
                break;
            case 3: // 删除一个字符
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                if (isLongPressDelete != true) {
                    isLongPressDelete = true;
                    handler.postDelayed(moveCursorDeleteTask, 500);
                }
                break;
            case 4: // 删除全部字符
                clear();
                break;
            case 5: // 换行
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                break;
            case 6: // 空格
            onreleased=false;
                if (couldinputspace) {
                    executorService = Executors.newSingleThreadExecutor();
                    future =
                            executorService.submit(
                                    () -> {
                                        try {
                                            Thread.sleep(500);
                                           if(onreleased!=true) {
                                           	couldinputspace = false;
                                            // 执行你的业务逻辑
                                            IatDemo.start();
                                           } 
                                        } catch (Exception err) {

                                        }
                                    });
                }

                break;
            case 7:
                for (Keyboard.Key key : keyboard.getKeys()) {
                    if (key.codes[0] == 7) {
                        if (key.on == true) {
                            autoclear = false;
                        } else {
                            autoclear = true;
                        }
                    }
                }
                break;
            case 8:
           InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showInputMethodPicker();
                }
                break;
            case 9:
                for (Keyboard.Key key : keyboard.getKeys()) {
                    if (key.codes[0] == 9) {
                        if (key.on == true) {
                            autoenter = false;
                        } else {
                            autoenter = true;
                        }
                    }
                }
                break;
            default:
                // 其他按键的功能
                break;
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        switch (primaryCode) {
            case 1:
                // ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
                stopLongPressLeft();
                break;

            case 2:
                // ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                stopLongPressRight();
                break;

            case 3:
                // ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                stopLongPressDelete();
                break;
            case 6:
                onreleased = true;
                try {
                    if (couldinputspace) {
                        ic.commitText(" ", 1);
                    } else {
                        IatDemo.stop();
                        couldinputspace = true;
                    }
                } catch (Exception err) {

                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {}

    @Override
    public void onText(CharSequence text) {}

    @Override
    public void swipeLeft() {}

    @Override
    public void swipeRight() {}

    @Override
    public void swipeDown() {}

    @Override
    public void swipeUp() {
        // 重写onKeyDown方法，用于监听物理按键的按下事件
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 如果按下的是KEYCODE_SY值120，设置isPressed为true
        if (keyCode == keycode) {
            if (windowShown && couldCatch) {
                turnoff();
                Catcher.startCatch();
            } else {
                if (couldCatch == false) {
                    // 提示
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // 如果按下的是KEYCODE_SY值120，设置isPressed为true
        if (keyCode == keycode) {
            Catcher.shouldprocess = true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public static void input(String string) {
        ic.commitText(string, 1);
    }

    public static void clear() {
//  
ic.setComposingText("", 0);
        ic.deleteSurroundingText(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static void enter() {
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
    }

    public static void turnon() {
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.codes[0] == 8) {
                // 更新按键标签
                key.label = "●"; // 或者 "○"，取决于您想要显示的标
                // key.on=true;
                keyboardView.invalidateAllKeys();
            }
        }
    }

    public static void turnoff() {
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.codes[0] == 8) {
                // 更新按键标签
                key.label = "○"; // 或者 "○"，取决于您想要显示的标签
                // key.on=false;
                keyboardView.invalidateAllKeys();
                // keyboardView.onKeyLongPress();

            }
        }
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        Catcher.cameraIndex = sharedPreferences.getInt("cameraIndex", 0); // Default value is 0
        keycode = sharedPreferences.getInt("keycode", 5); // Default value is 0
        Catcher.fps = sharedPreferences.getInt("fps", 15);
        Catcher.dy = sharedPreferences.getInt("dy", 10);
        //Catcher.imgWidth = (double) sharedPreferences.getInt("imgWidth", 75);
        Catcher.templateWidth = sharedPreferences.getInt("templateWidth", 40);
        Catcher.threshold = sharedPreferences.getInt("threshold", 10); // Default value is 0
        Catcher.matchdegree = sharedPreferences.getInt("matchdegree", 70);
        ifleft = sharedPreferences.getBoolean("ifleft", false); // Default value is false
        Catcher.filePath = sharedPreferences.getString("filepath","/sdcard/Pictures/tmp.jpg");
        Catcher.ocrapi = sharedPreferences.getInt("ocrapi", 1);
        
        

        // Use these values as needed in your application
    }

    private void stopLongPressLeft() {
        if (isLongPressLeft) {
            isLongPressLeft = false;
            handler.removeCallbacks(moveCursorLeftTask);
        }
    }

    private void stopLongPressRight() {
        if (isLongPressRight) {
            isLongPressRight = false;
            handler.removeCallbacks(moveCursorRightTask);
        }
    }

    private void stopLongPressDelete() {
        if (isLongPressDelete) {
            isLongPressDelete = false;
            handler.removeCallbacks(moveCursorDeleteTask);
        }
    }
}
