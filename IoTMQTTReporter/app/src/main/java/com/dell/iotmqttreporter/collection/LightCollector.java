package com.dell.iotmqttreporter.collection;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;

public class LightCollector implements SensorEventListener {

    private static final String TAG = "LightChange";

    // required differences between current and last readings in order to send update
    private final static float CHG_LIGHT_LEVEL = 3;

    private float lastLightLevel;

    private Context ctx;

    public LightCollector(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        HashMap<ReportLevel, String> lightUpdateMap = new HashMap<>();
        float lightLevel = event.values[0];
        if (Math.abs(lightLevel - lastLightLevel) > CHG_LIGHT_LEVEL) {
            Log.d(TAG, "Significant light level change detected:  " + lightLevel);
            lightUpdateMap.put(ReportLevel.lightlevel, Float.toString(lightLevel));
            lastLightLevel = lightLevel;

            sendMessage(lightUpdateMap);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void sendMessage(HashMap<ReportLevel, String> lightUpdateMap) {
        Intent updateIntent = new Intent();
        updateIntent.setAction("com.dell.iot.android.update");
        updateIntent.putExtra("updates", lightUpdateMap);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent);
    }
}
