package com.example.svoboda;

/*
 * Copyright 2017 Maxst, Inc. All Rights Reserved.
 */

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.maxst.ar.MaxstAR;
import com.maxst.ar.TrackerManager;

public abstract class ARActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaxstAR.init(getApplicationContext(),"bDc1FSIla8l3BiVEeto3cWp53VWROVpz+QlZSV0Auf8=");
        MaxstAR.setScreenOrientation(getResources().getConfiguration().orientation);

        String sdkVersion = MaxstAR.getVersion();
        Log.d("ARActivity", "SDK Version : " + sdkVersion);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TrackerManager.getInstance().destroyTracker();
        MaxstAR.deinit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }

        MaxstAR.setScreenOrientation(newConfig.orientation);
    }
}
