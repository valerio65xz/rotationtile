package com.bin.mirrormate;

import android.content.SharedPreferences;
import android.service.quicksettings.TileService;

public class RotationTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        RotationHelper.updateTile(this);
    }

    @Override
    public void onClick() {
        super.onClick();
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        RotationHelper.execute(this, prefs, this);
    }

}