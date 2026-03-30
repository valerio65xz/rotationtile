package com.example.rotationtile;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class RotationHelper {

    private static final int ROTATION_PORTRAIT = 0;
    private static final int ROTATION_LANDSCAPE = 1;

    public static void toggleRotation(Context context) {
        ContentResolver cr = context.getContentResolver();

        Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0);

        int current = Settings.System.getInt(cr, Settings.System.USER_ROTATION, ROTATION_PORTRAIT);
        int next = (current == ROTATION_PORTRAIT) ? ROTATION_LANDSCAPE : ROTATION_PORTRAIT;

        Settings.System.putInt(cr, Settings.System.USER_ROTATION, next);
    }

    public static void updateTile(TileService service) {
        Tile tile = service.getQsTile();
        if (tile == null) return;

        int rotation = Settings.System.getInt(
                service.getContentResolver(),
                Settings.System.USER_ROTATION,
                ROTATION_PORTRAIT);

        if (rotation == ROTATION_LANDSCAPE) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel(service.getString(R.string.tile_landscape));
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel(service.getString(R.string.tile_portrait));
        }

        tile.setIcon(Icon.createWithResource(service, R.drawable.ic_rotation));
        tile.updateTile();
    }

    public static void collapseStatusBar(TileService service) {
        Intent intent = new Intent(service, DismissActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent pending = PendingIntent.getActivity(
                    service,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );
            service.startActivityAndCollapse(pending);
        } else {
            service.startActivityAndCollapse(intent);
        }
    }

    public static void collapseFromContext(Context context) {
        Intent intent = new Intent(context, DismissActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void adjustBrightness(Context context, SharedPreferences prefs) {
        ContentResolver contentResolver = context.getContentResolver();

        int current = Settings.System.getInt(
                contentResolver,
                Settings.System.USER_ROTATION,
                ROTATION_PORTRAIT
        );
        int brightness = (current == ROTATION_LANDSCAPE)
                ? prefs.getInt(MainActivity.PREF_BRIGHTNESS_LANDSCAPE, 50)
                : prefs.getInt(MainActivity.PREF_BRIGHTNESS_PORTRAIT, 128);

        Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        );
        Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness
        );
    }

    public static void execute(Context context, SharedPreferences prefs, TileService tileService) {
        toggleRotation(context);

        if (tileService != null) {
            updateTile(tileService);
        }
        if (prefs.getBoolean(MainActivity.PREF_BRIGHTNESS_ENABLED, false)) {
            adjustBrightness(context, prefs);
        }
        if (prefs.getBoolean(MainActivity.PREF_COLLAPSE, true)) {
            if (tileService != null) {
                collapseStatusBar(tileService);
            } else {
                collapseFromContext(context);
            }
        }
    }

}