package com.example.rotationtile;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class VolumeButtonService extends AccessibilityService {

    public static final String PREF_VOLUME_TRIGGER = "volume_trigger_enabled";

    private static final int REQUIRED_PRESSES = 5;

    // Volume down counters
    private int pressCountDown = 0;
    private long firstPressTimeDown = 0;

    // Volume up counters
    private int pressCountUp = 0;
    private long firstPressTimeUp = 0;

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

        if (!prefs.getBoolean(PREF_VOLUME_TRIGGER, false)) {
            return false;
        }

        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }

        long resetIntervalMs = prefs.getInt(MainActivity.PREF_VOLUME_TRIGGER_WINDOW_MS, 2000);
        long now = System.currentTimeMillis();

        // ── Volume down: toggle rotation ──────────────────────────────────────
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (now - firstPressTimeDown > resetIntervalMs) {
                pressCountDown = 0;
                firstPressTimeDown = now;
            }

            pressCountDown++;

            if (pressCountDown >= REQUIRED_PRESSES) {
                pressCountDown = 0;
                firstPressTimeDown = 0;
                onFivePressesDownDetected();
                return true; // consume event — volume won't change
            }
        }

        // ── Volume up: wake voice assistant ───────────────────────────────────
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            if (now - firstPressTimeUp > resetIntervalMs) {
                pressCountUp = 0;
                firstPressTimeUp = now;
            }

            pressCountUp++;

            if (pressCountUp >= REQUIRED_PRESSES) {
                pressCountUp = 0;
                firstPressTimeUp = 0;
                onFivePressesUpDetected();
                return true; // consume event — volume won't change
            }
        }

        return false;
    }

    // ── actions ───────────────────────────────────────────────────────────────

    private void onFivePressesDownDetected() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        RotationHelper.execute(this, prefs, null);
    }

    private void onFivePressesUpDetected() {
        Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}