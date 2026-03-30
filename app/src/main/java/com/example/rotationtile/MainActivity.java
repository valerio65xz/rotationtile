package com.example.rotationtile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "RotationTilePrefs";
    public static final String PREF_COLLAPSE = "collapse_on_toggle";
    public static final String PREF_BRIGHTNESS = "min_brightness_on_toggle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Settings.System.canWrite(this)) {
            showPermissionDialog();
        } else if (!isAccessibilityEnabled()) {
            openAccessibilitySettings();
        } else {
            showSettingsDialog();
        }
    }

    private boolean isAccessibilityEnabled() {
        String service = getPackageName() + "/" + VolumeButtonService.class.getName();

        try {
            int enabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED,
                    0
            );
            if (enabled == 0) return false;

            String enabledServices = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );

            return enabledServices != null && enabledServices.contains(service);
        } catch (Exception e) {
            return false;
        }
    }

    // ── dialogs ──────────────────────────────────────────────────────────────

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Permissions required")
                .setMessage("This app needs two permissions to work:\n\n" +
                        "1. Modify system settings — to control screen rotation and brightness.\n\n" +
                        "2. Accessibility service — to detect 5x volume down button trigger.\n\n" +
                        "You will be taken to each permission screen one after the other.")
                .setCancelable(false)
                .setPositiveButton("Continue", (d, w) -> openWriteSettings())
                .setNegativeButton("Cancel", (d, w) -> finish())
                .show();
    }

    private void openWriteSettings() {
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + getPackageName())
        );
        startActivity(intent);
    }

    private void openAccessibilitySettings() {
        new AlertDialog.Builder(this)
                .setTitle("🔧 Accessibility permission")
                .setMessage("Now enable \"Volume Trigger\" in the accessibility settings to allow the 5x volume down shortcut.")
                .setCancelable(false)
                .setPositiveButton("Open Accessibility", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Skip", (d, w) -> {}) // user can skip, feature just won't work
                .show();
    }

    private void showSettingsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Build layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(20);
        layout.setPadding(padding, padding, padding, 0);

        // Description
        TextView description = new TextView(this);
        description.setText(getString(R.string.settings_description));
        description.setTextSize(14);
        description.setPadding(0, 0, 0, dpToPx(16));
        layout.addView(description);

        // Checkbox 1 — collapse panel
        CheckBox checkCollapse = new CheckBox(this);
        checkCollapse.setText(getString(R.string.pref_collapse));
        checkCollapse.setChecked(prefs.getBoolean(PREF_COLLAPSE, false));
        checkCollapse.setPadding(0, 0, 0, dpToPx(4));
        layout.addView(checkCollapse);

        TextView collapseHint = new TextView(this);
        collapseHint.setText(getString(R.string.collapse_hint));
        collapseHint.setTextSize(12);
        collapseHint.setAlpha(0.6f);
        collapseHint.setPadding(dpToPx(32), 0, 0, dpToPx(16));
        layout.addView(collapseHint);

        // Checkbox 2 — minimum brightness
        CheckBox checkBrightness = new CheckBox(this);
        checkBrightness.setText(getString(R.string.pref_brightness));
        checkBrightness.setChecked(prefs.getBoolean(PREF_BRIGHTNESS, false));
        checkBrightness.setPadding(0, 0, 0, dpToPx(4));
        layout.addView(checkBrightness);

        TextView brightnessHint = new TextView(this);
        brightnessHint.setText(getString(R.string.brightness_hint));
        brightnessHint.setTextSize(12);
        brightnessHint.setAlpha(0.6f);
        brightnessHint.setPadding(dpToPx(32), 0, 0, dpToPx(8));
        layout.addView(brightnessHint);

        // Checkbox 3 — volume trigger
        CheckBox checkVolume = new CheckBox(this);
        checkVolume.setText(getString(R.string.check_volume));
        checkVolume.setChecked(prefs.getBoolean(VolumeButtonService.PREF_VOLUME_TRIGGER, false));
        checkVolume.setPadding(0, 0, 0, dpToPx(4));
        layout.addView(checkVolume);

        TextView volumeHint = new TextView(this);
        volumeHint.setText(getString(R.string.volume_hint));
        volumeHint.setTextSize(12);
        volumeHint.setAlpha(0.6f);
        volumeHint.setPadding(dpToPx(32), 0, 0, dpToPx(8));
        layout.addView(volumeHint);

        new AlertDialog.Builder(this)
                .setTitle("⚙️ Rotation Tile Settings")
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("Save & Close", (d, w) -> {
                    prefs.edit()
                            .putBoolean(PREF_COLLAPSE, checkCollapse.isChecked())
                            .putBoolean(PREF_BRIGHTNESS, checkBrightness.isChecked())
                            .putBoolean(VolumeButtonService.PREF_VOLUME_TRIGGER, checkVolume.isChecked())
                            .apply();
                    finish();
                })
                .show();
    }

    // ── utils ─────────────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

}