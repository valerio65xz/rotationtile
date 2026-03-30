package com.example.rotationtile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "RotationTilePrefs";
    public static final String PREF_COLLAPSE = "collapse_on_toggle";
    public static final String PREF_BRIGHTNESS_ENABLED = "brightness_enabled";
    public static final String PREF_BRIGHTNESS_PORTRAIT = "brightness_portrait";
    public static final String PREF_BRIGHTNESS_LANDSCAPE = "brightness_landscape";

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

    // ── permission dialogs ────────────────────────────────────────────────────

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Permissions required")
                .setMessage("""
                        This app needs two permissions to work:
                        
                        1. Modify system settings — to control screen rotation and brightness.
                        
                        2. Accessibility service — to detect 5x volume down button trigger.
                        
                        You will be taken to each permission screen one after the other.""")
                .setCancelable(false)
                .setPositiveButton("Continue", (d, w) -> openWriteSettings())
                .setNegativeButton("Cancel", (d, w) -> finish())
                .show();
    }

    private void openWriteSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + getPackageName()));
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
                .setNegativeButton("Skip", (d, w) -> {})
                .show();
    }

    // ── settings dialog ───────────────────────────────────────────────────────

    private void showSettingsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = dpToPx(20);
        layout.setPadding(p, p, p, 0);

        TextView description = new TextView(this);
        description.setText(getString(R.string.settings_description));
        description.setTextSize(14);
        description.setPadding(0, 0, 0, dpToPx(16));
        layout.addView(description);

        // ── Checkbox: collapse panel ──────────────────────────────────────────
        CheckBox checkCollapse = new CheckBox(this);
        checkCollapse.setText(getString(R.string.pref_collapse));
        checkCollapse.setChecked(prefs.getBoolean(PREF_COLLAPSE, false));
        layout.addView(checkCollapse);

        addHint(layout, "Automatically dismisses Quick Settings when the tile is tapped.");

        // ── Checkbox: brightness ──────────────────────────────────────────────
        CheckBox checkBrightness = new CheckBox(this);
        checkBrightness.setText(getString(R.string.pref_brightness));
        checkBrightness.setChecked(prefs.getBoolean(PREF_BRIGHTNESS_ENABLED, false));
        layout.addView(checkBrightness);

        addHint(layout, "Applies a different brightness level when switching to portrait or landscape.");

        // Portrait brightness row
        LinearLayout portraitRow = buildBrightnessRow("Portrait brightness (0–255):",
                prefs.getInt(PREF_BRIGHTNESS_PORTRAIT, 128));
        EditText portraitInput = (EditText) portraitRow.getChildAt(1);
        layout.addView(portraitRow);

        // Landscape brightness row
        LinearLayout landscapeRow = buildBrightnessRow("Landscape brightness (0–255):",
                prefs.getInt(PREF_BRIGHTNESS_LANDSCAPE, 50));
        EditText landscapeInput = (EditText) landscapeRow.getChildAt(1);
        layout.addView(landscapeRow);

        // Show/hide brightness inputs based on checkbox
        int inputVisibility = checkBrightness.isChecked()
                ? android.view.View.VISIBLE : android.view.View.GONE;
        portraitRow.setVisibility(inputVisibility);
        landscapeRow.setVisibility(inputVisibility);

        checkBrightness.setOnCheckedChangeListener((btn, isChecked) -> {
            portraitRow.setVisibility(isChecked
                    ? android.view.View.VISIBLE : android.view.View.GONE);
            landscapeRow.setVisibility(isChecked
                    ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        // ── Checkbox: volume trigger ──────────────────────────────────────────
        CheckBox checkVolume = new CheckBox(this);
        checkVolume.setText(getString(R.string.check_volume));
        checkVolume.setChecked(prefs.getBoolean(VolumeButtonService.PREF_VOLUME_TRIGGER, false));
        layout.addView(checkVolume);

        addHint(layout, "Press volume down 5 times quickly to toggle rotation without opening Quick Settings.");

        // ── Save ──────────────────────────────────────────────────────────────
        new AlertDialog.Builder(this)
                .setTitle("⚙️ Rotation Tile Settings")
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("Save & Close", (d, w) -> {
                    int portraitBrightness  = clampBrightness(portraitInput.getText().toString(), 128);
                    int landscapeBrightness = clampBrightness(landscapeInput.getText().toString(), 50);

                    prefs.edit()
                            .putBoolean(PREF_COLLAPSE, checkCollapse.isChecked())
                            .putBoolean(PREF_BRIGHTNESS_ENABLED, checkBrightness.isChecked())
                            .putInt(PREF_BRIGHTNESS_PORTRAIT, portraitBrightness)
                            .putInt(PREF_BRIGHTNESS_LANDSCAPE, landscapeBrightness)
                            .putBoolean(VolumeButtonService.PREF_VOLUME_TRIGGER, checkVolume.isChecked())
                            .apply();
                    finish();
                })
                .show();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private LinearLayout buildBrightnessRow(String label, int currentValue) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(32), dpToPx(4), 0, dpToPx(8));

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(13);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tv.setLayoutParams(tvParams);
        row.addView(tv);

        EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(currentValue));
        et.setSelectAllOnFocus(true);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                dpToPx(60), LinearLayout.LayoutParams.WRAP_CONTENT);
        et.setLayoutParams(etParams);
        row.addView(et);

        return row;
    }

    private void addHint(LinearLayout parent, String text) {
        TextView hint = new TextView(this);
        hint.setText(text);
        hint.setTextSize(12);
        hint.setAlpha(0.6f);
        hint.setPadding(dpToPx(32), 0, 0, dpToPx(16));
        parent.addView(hint);
    }

    private int clampBrightness(String value, int defaultValue) {
        try {
            return Math.min(255, Math.max(0, Integer.parseInt(value)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean isAccessibilityEnabled() {
        String service = getPackageName() + "/" + VolumeButtonService.class.getName();
        try {
            int enabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED, 0);
            if (enabled == 0) return false;
            String enabledServices = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return enabledServices != null && enabledServices.contains(service);
        } catch (Exception e) {
            return false;
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}