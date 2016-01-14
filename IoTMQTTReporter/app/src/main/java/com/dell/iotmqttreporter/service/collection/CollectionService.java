/*******************************************************************************
 * Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.service.collection;

import static com.dell.iotmqttreporter.service.collection.CollectionConstants.*;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dell.iotmqttreporter.collection.LastCollected;
import com.dell.iotmqttreporter.collection.ReportKey;
import com.dell.iotmqttreporter.collection.collector.BatteryCollector;
import com.dell.iotmqttreporter.collection.collector.GeoCollector;
import com.dell.iotmqttreporter.collection.collector.LightCollector;
import com.dell.iotmqttreporter.collection.collector.OrientationCollector;

/**
 * Created by Jim on 1/10/2016.
 * <p/>
 * Service responsible for kicking off device data collection - more precisely for kicking of the various broadcast receivers and listeners (the collectors) that capture the device data.
 * Also starts the CollectionUpdateSendor (a broadcast receiver) to snag new data collection intents and send out the collected data from the collector listeners/broadcasters.
 * Responsible for shutdown and cleanup of the collectors and the sendor broadcast receiver when the service is stopped.
 */
public class CollectionService extends Service {

    private static final String TAG = "CollectionService";

    // the "collector" listeners and broadcast receivers
    private GeoCollector geoCollector;
    private OrientationCollector orientationCollector;
    private LightCollector lightCollector;
    private BatteryCollector batteryCollector;
    // the data sending broadcast receiver
    private CollectionUpdateSendor sendor;

    /**
     * Create the broadcast receiver that sends new MQTT messages on collected data
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sendor = new CollectionUpdateSendor();
        LocalBroadcastManager.getInstance(this).registerReceiver(sendor,
                new IntentFilter(UPDATE_COLLECTION_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting all collection services");
        startBatteryService();
        startLightService();
        startLocationService();
        startOrientationService();
        updateCollectingState(1);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBatteryService();
        stopLightService();
        stopLocationService();
        stopOrientationService();
        updateCollectingState(0);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sendor);
        Log.d(TAG, "Stopped all services");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void startBatteryService() {
        batteryCollector = new BatteryCollector();
        this.registerReceiver(batteryCollector, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void stopBatteryService() {
        if (batteryCollector != null) {
            this.unregisterReceiver(batteryCollector);
        }
    }

    private void startLightService() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightCollector = new LightCollector(this.getApplicationContext());
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(lightCollector, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopLightService() {
        if (lightCollector != null) {
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(lightCollector);
        }
    }

    private void startLocationService() {
        LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        geoCollector = new GeoCollector(this.getApplicationContext());

        if (!checkForPermission(FINE_LOC_PERMISSION)) {
            geoCollector.setLastLocation(locMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_INTERVAL, MIN_DISTANCE, geoCollector);
        } else {
            geoCollector.setLastLocation(locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL, MIN_DISTANCE, geoCollector);
        }
    }

    private void stopLocationService() {
        if (geoCollector != null && checkForPermission(FINE_LOC_PERMISSION)) {
            LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
            locMgr.removeUpdates(geoCollector);
        }
    }

    private void startOrientationService() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientationCollector = new OrientationCollector(this.getApplicationContext());
        Sensor sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(orientationCollector, sensorAccel,
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(orientationCollector, sensorGravity,
                SensorManager.SENSOR_DELAY_UI);
    }

    private void stopOrientationService() {
        if (orientationCollector != null) {
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(orientationCollector);
        }
    }

    private boolean checkForPermission(String permission) {
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void updateCollectingState(int on){
        LastCollected.getInstance().put(ReportKey.collect, on);
    }

}
