package com.example.rotationtile;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No layout needed — we just show a dialog then close
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Settings.System.canWrite(this)) {
            showDialog(
                    "✅ Permission granted!",
                    """
                            You can now add the "Rotate Screen" tile to your Quick Settings panel:
                            
                            1. Pull down the notification shade
                            2. Tap the pencil / edit icon
                            3. Find "Rotate Screen" and drag it to your tiles
                            
                            Tap the tile anytime to toggle between portrait and landscape.""",
                    false
            );
        } else {
            showDialog(
                    "⚠️ Permission required",
                    """
                            This app needs the "Modify system settings" permission to lock your screen rotation.
                            
                            Tap OK to open the permission screen, then enable the toggle for this app.""",
                    true
            );
        }
    }

    private void showDialog(String title, String message, boolean openSettings) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false);

        if (openSettings) {
            builder.setPositiveButton("Open Settings", (d, w) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            });
            builder.setNegativeButton("Cancel", (d, w) -> finish());
        } else {
            builder.setPositiveButton("Close", (d, w) -> finish());
        }

        builder.show();
    }
}