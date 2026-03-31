package com.bin.mirrormate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "RotationTilePrefs";
    public static final String PREF_COLLAPSE = "collapse_on_toggle";
    public static final String PREF_BRIGHTNESS_ENABLED = "brightness_enabled";
    public static final String PREF_BRIGHTNESS_PORTRAIT = "brightness_portrait";
    public static final String PREF_BRIGHTNESS_LANDSCAPE = "brightness_landscape";
    public static final String PREF_VOLUME_TRIGGER_WINDOW_MS = "volume_trigger_window_ms";

    private AlertDialog activeDialog = null;
    private SharedPreferences prefs;

    // Views
    private CheckBox checkCollapse;
    private CheckBox checkBrightness;
    private LinearLayout layoutBrightness;
    private SeekBar seekPortrait;
    private SeekBar seekLandscape;
    private CheckBox checkVolume;
    private LinearLayout layoutVolumeWindow;
    private SeekBar seekVolumeWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If accessibility was revoked, uncheck volume trigger
        if (!isAccessibilityEnabled()) {
            prefs.edit()
                    .putBoolean(VolumeButtonService.PREF_VOLUME_TRIGGER, false)
                    .apply();
        }

        if (!Settings.System.canWrite(this)) {
            showDialog(buildPermissionDialog());
            return;
        }

        // If the volume trigger dialog is showing and accessibility just got granted
        if (activeDialog != null && activeDialog.isShowing() && isAccessibilityEnabled()) {
            activeDialog.dismiss();
            prefs.edit()
                    .putBoolean(VolumeButtonService.PREF_VOLUME_TRIGGER, true)
                    .apply();
            checkVolume.setChecked(true);
            layoutVolumeWindow.setVisibility(View.VISIBLE);
            return;
        }

        // First time showing the main screen
        if (checkCollapse == null) {
            showMainScreen();
        }
    }

    // ── dialog guard ──────────────────────────────────────────────────────────

    private void showDialog(AlertDialog.Builder builder) {
        if (activeDialog != null && activeDialog.isShowing()) {
            return;
        }
        activeDialog = builder.create();
        activeDialog.setOnDismissListener(d -> activeDialog = null);
        activeDialog.show();
    }

    // ── permission dialog ─────────────────────────────────────────────────────

    private AlertDialog.Builder buildPermissionDialog() {
        return new AlertDialog.Builder(this)
                .setTitle("⚠️ Permission required")
                .setMessage("""
                        This app needs the "Modify system settings" permission
                        to control screen rotation and brightness.
                        
                        Tap Continue to open the permission screen,
                        then enable the toggle for this app.""")
                .setCancelable(false)
                .setPositiveButton("Continue", (d, w) -> openWriteSettings())
                .setNegativeButton("Cancel", (d, w) -> finish());
    }

    // ── main screen ───────────────────────────────────────────────────────────

    private void showMainScreen() {
        setContentView(R.layout.activity_main);
        bindViews();
        loadPreferences();
        attachListeners();
    }

    private void bindViews() {
        checkCollapse      = findViewById(R.id.check_collapse);
        checkBrightness    = findViewById(R.id.check_brightness);
        layoutBrightness   = findViewById(R.id.layout_brightness);
        seekPortrait       = findViewById(R.id.seek_brightness_portrait);
        seekLandscape      = findViewById(R.id.seek_brightness_landscape);
        checkVolume        = findViewById(R.id.check_volume);
        layoutVolumeWindow = findViewById(R.id.layout_volume_window);
        seekVolumeWindow   = findViewById(R.id.seek_volume_window);
    }

    private void loadPreferences() {
        checkCollapse.setChecked(prefs.getBoolean(PREF_COLLAPSE, false));

        boolean brightnessEnabled = prefs.getBoolean(PREF_BRIGHTNESS_ENABLED, false);
        checkBrightness.setChecked(brightnessEnabled);
        layoutBrightness.setVisibility(brightnessEnabled ? View.VISIBLE : View.GONE);
        seekPortrait.setProgress(prefs.getInt(PREF_BRIGHTNESS_PORTRAIT, 128));
        seekLandscape.setProgress(prefs.getInt(PREF_BRIGHTNESS_LANDSCAPE, 128));

        boolean volumeEnabled = prefs.getBoolean(VolumeButtonService.PREF_VOLUME_TRIGGER, false);
        checkVolume.setChecked(volumeEnabled);
        layoutVolumeWindow.setVisibility(volumeEnabled ? View.VISIBLE : View.GONE);
        // SeekBar offset: actual ms = progress + 500, so 1500ms default = progress 1000, half = 4750ms
        seekVolumeWindow.setProgress(prefs.getInt(PREF_VOLUME_TRIGGER_WINDOW_MS, 2000) - 500);
    }

    private void attachListeners() {
        // Collapse
        checkCollapse.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean(PREF_COLLAPSE, isChecked).apply());

        // Brightness checkbox
        checkBrightness.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(PREF_BRIGHTNESS_ENABLED, isChecked).apply();
            layoutBrightness.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Brightness seekbars
        seekPortrait.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser) prefs.edit().putInt(PREF_BRIGHTNESS_PORTRAIT, progress).apply();
            }
        });
        seekLandscape.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser) prefs.edit().putInt(PREF_BRIGHTNESS_LANDSCAPE, progress).apply();
            }
        });

        // Volume trigger checkbox — intercept and show permission dialog
        checkVolume.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                btn.setChecked(false); // revert until permissions confirmed
                showVolumeTriggerPermissionDialog();
            } else {
                prefs.edit().putBoolean(VolumeButtonService.PREF_VOLUME_TRIGGER, false).apply();
                layoutVolumeWindow.setVisibility(View.GONE);
            }
        });

        // Volume window seekbar — offset by 500ms
        seekVolumeWindow.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser) prefs.edit()
                        .putInt(PREF_VOLUME_TRIGGER_WINDOW_MS, progress + 500)
                        .apply();
            }
        });

        TextView tvVolumeWindowValue = findViewById(R.id.tv_volume_window_value);

        // Set initial value text
        int initialMs = prefs.getInt(PREF_VOLUME_TRIGGER_WINDOW_MS, 2000);
        tvVolumeWindowValue.setText(getString(R.string.current_value_ms, initialMs));

        seekVolumeWindow.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                int ms = progress + 500;
                tvVolumeWindowValue.setText(getString(R.string.current_value_ms, ms));
                if (fromUser) prefs.edit()
                        .putInt(PREF_VOLUME_TRIGGER_WINDOW_MS, ms)
                        .apply();
            }
        });
    }

    // ── volume trigger permission dialog ──────────────────────────────────────

    private void showVolumeTriggerPermissionDialog() {
        // If already granted, enable directly without showing dialog
        if (isAccessibilityEnabled()) {
            prefs.edit()
                    .putBoolean(VolumeButtonService.PREF_VOLUME_TRIGGER, true)
                    .apply();
            checkVolume.setChecked(true);
            layoutVolumeWindow.setVisibility(View.VISIBLE);
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = dpToPx(20);
        layout.setPadding(p, p, p, p);

        boolean fromPlayStore = isInstalledFromPlayStore();
        int stepNumber = 1;

        // ── Step 1 (sideloaded only): Allow restricted settings ───────────────
        if (!fromPlayStore) {
            addSectionTitle(layout, "Step " + stepNumber++ + " — Allow restricted settings");
            addBody(layout,
                    """
                    Since this app was installed outside the Play Store, Android \
                    restricts access to the Accessibility service.
    
                    Follow these steps to unlock it:
    
                    ① Open your phone's Settings
                       → Apps → App Management → Rotation Tile
                       → Tap the ⋮ three-dot menu → "Allow restricted settings"
    
                    ⚠️ If the ⋮ menu doesn't appear:
                       → Click on "Open Accessibility" → Downloaded Apps
                       → Tap "Volume Trigger" — a "Restricted setting" popup will appear
                       → Tap OK and fully close this app
                       → Then go to Settings → Apps → App Management → Rotation Tile
                       → The ⋮ menu will now appear
                       → Tap it → "Allow restricted settings"
                       → You can now come back to this app
    
                    ③ Once allowed, continue to the next step.""");
            addDivider(layout);
        }

        // ── Step 2 (always): Enable accessibility service ─────────────────────
        addSectionTitle(layout, "Step " + stepNumber + " — Enable Accessibility service");
        addBody(layout,
                """
                        Go to Settings → Accessibility → Downloaded Apps
                        → tap "Volume Trigger" and enable it.
                        
                        Once enabled, come back here — the checkbox will activate automatically.""");
        addGoToButton(layout, "Open Accessibility →", this::openAccessibilitySettings);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("🔧 Volume trigger setup")
                .setView(scrollView)
                .setCancelable(false)
                .setPositiveButton("Close", (d, w) -> checkVolume.setChecked(false))
                .setNegativeButton(null, null);

        showDialog(builder);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void openWriteSettings() {
        startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + getPackageName())));
    }

    private void openAccessibilitySettings() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    private void addSectionTitle(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(0, dpToPx(8), 0, dpToPx(6));
        parent.addView(tv);
    }

    private void addBody(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setPadding(0, 0, 0, dpToPx(10));
        parent.addView(tv);
    }

    private void addGoToButton(LinearLayout parent, String label, Runnable action) {
        android.widget.Button btn = new android.widget.Button(this);
        btn.setText(label);
        btn.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpToPx(12);
        btn.setLayoutParams(params);
        parent.addView(btn);
    }

    private void addDivider(LinearLayout parent) {
        View divider = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        params.topMargin = dpToPx(8);
        params.bottomMargin = dpToPx(16);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(0x22888888);
        parent.addView(divider);
    }

    private boolean isInstalledFromPlayStore() {
        if (Build.VERSION.SDK_INT < 30) return false;
        try {
            String installer = getPackageManager()
                    .getInstallSourceInfo(getPackageName())
                    .getInitiatingPackageName();
            return "com.android.vending".equals(installer);
        } catch (Exception e) {
            return false;
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

    // ── SimpleSeekBarListener ─────────────────────────────────────────────────

    private abstract static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }

}