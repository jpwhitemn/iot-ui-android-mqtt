/*******************************************************************************
 * Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.collection.collector;

import static com.dell.iotmqttreporter.service.collection.CollectionConstants.*;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dell.iotmqttreporter.collection.LastCollected;
import com.dell.iotmqttreporter.collection.ReportKey;

import java.util.HashMap;

/**
 * Created by Jim on 1/10/2016.
 * <p/>
 * Listener to orientation/direction data changes provided by the Android device.
 * <p/>
 * On a detected change, sends a map containing the orienteation data that changed to the CollectionUpdateSendor (to send data out via MQTT).
 * <p/>
 * This listener is started/registered and unregistered by the CollectionService.
 */
public class OrientationCollector implements SensorEventListener {

    private static final String TAG = "OrientationChange";

    // required differences between current and last readings in order to send update
    private static final int CHG_DIR_DIFF = 20;

    private int lastDirection;  // in degrees (-180 to 180)

    private float[] mGravity;
    private float[] mGeomagnetic;

    private Context ctx;

    public OrientationCollector(Context ctx) {
        this.ctx = ctx;
    }

    public void onSensorChanged(SensorEvent event) {
        HashMap<ReportKey, String> orientUpdateMap = new HashMap<>();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                // orientation contains azimut, pitch and roll
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                int direction = Math.round(orientation[0] * 360 / (2 * 3.14159f));
                if (Math.abs(direction - lastDirection) > CHG_DIR_DIFF) {
                    Log.d(TAG, "Significant change in direction detected:  " + direction);
                    orientUpdateMap.put(ReportKey.direction, Integer.toString(direction));
                    lastDirection = direction;
                    LastCollected.put(ReportKey.direction, lastDirection);
                    sendMessage(orientUpdateMap);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void sendMessage(HashMap<ReportKey, String> lightUpdateMap) {
        Intent updateIntent = new Intent();
        updateIntent.setAction(UPDATE_COLLECTION_ACTION);
        updateIntent.putExtra(INTENT_UPD_KEY, lightUpdateMap);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent);
    }
}
