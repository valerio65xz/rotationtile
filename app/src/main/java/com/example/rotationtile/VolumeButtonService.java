package com.example.rotationtile;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class VolumeButtonService extends AccessibilityService {

    public static final String PREF_VOLUME_TRIGGER = "volume_trigger_enabled";

    private static final int REQUIRED_PRESSES = 5;
    private static final long RESET_INTERVAL_MS = 1000; // 2 seconds window

    private int pressCount = 0;
    private long firstPressTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // not needed
    }

    @Override
    public void onInterrupt() {
        // not needed
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);

        // If feature is disabled, pass the event through normally
        if (!prefs.getBoolean(PREF_VOLUME_TRIGGER, false)) {
            return false;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            long now = System.currentTimeMillis();

            // Reset counter if window has expired
            if (now - firstPressTime > RESET_INTERVAL_MS) {
                pressCount = 0;
                firstPressTime = now;
            }

            pressCount++;

            if (pressCount >= REQUIRED_PRESSES) {
                pressCount = 0;
                firstPressTime = 0;
                onFivePressesDetected();
                return true; // consume the event — volume won't change
            }
        }

        return false; // let the system handle it normally
    }

    private void onFivePressesDetected() {
        ContentResolver cr = getContentResolver();

        // Read current rotation and toggle it
        Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0);
        int current = Settings.System.getInt(
                cr, Settings.System.USER_ROTATION, 0);
        int next = (current == 0) ? 1 : 0;
        Settings.System.putInt(cr, Settings.System.USER_ROTATION, next);
    }
}