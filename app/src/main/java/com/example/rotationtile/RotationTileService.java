package com.example.rotationtile;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * Quick Settings tile that toggles screen rotation between
 * landscape (forced) and portrait (forced).
 * <p>
 * WRITE_SETTINGS permission must be granted via MainActivity first.
 */
public class RotationTileService extends TileService {

    // Values for Settings.System.USER_ROTATION
    private static final int ROTATION_PORTRAIT   = 0; // 0°
    private static final int ROTATION_LANDSCAPE  = 1; // 90°

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        toggleRotation();
        updateTile();
        collapseStatusBar();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void toggleRotation() {
        ContentResolver cr = getContentResolver();

        // First: disable auto-rotate so our forced value actually sticks
        Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0);

        int current = Settings.System.getInt(cr, Settings.System.USER_ROTATION, ROTATION_PORTRAIT);
        int next    = (current == ROTATION_PORTRAIT) ? ROTATION_LANDSCAPE : ROTATION_PORTRAIT;

        Settings.System.putInt(cr, Settings.System.USER_ROTATION, next);
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;

        ContentResolver cr = getContentResolver();
        int rotation = Settings.System.getInt(cr, Settings.System.USER_ROTATION, ROTATION_PORTRAIT);

        if (rotation == ROTATION_LANDSCAPE) {
            tile.setState(Tile.STATE_ACTIVE);
            tile.setLabel(getString(R.string.tile_landscape));
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel(getString(R.string.tile_portrait));
        }

        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_rotation));
        tile.updateTile();
    }

    private void collapseStatusBar() {
        Intent intent = new Intent(this, DismissActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent pending = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );
            startActivityAndCollapse(pending);
        } else {
            startActivityAndCollapse(intent); // deprecated but only way on Android < 14
        }
    }

}