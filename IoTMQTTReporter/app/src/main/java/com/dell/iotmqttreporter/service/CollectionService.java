package com.dell.iotmqttreporter.service;

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

import com.dell.iotmqttreporter.collection.BatteryCollector;
import com.dell.iotmqttreporter.collection.GeoCollector;
import com.dell.iotmqttreporter.collection.LightCollector;
import com.dell.iotmqttreporter.collection.OrientationCollector;

public class CollectionService extends Service {

    private static final String TAG = "CollectionService";
    private static final int LOCATION_INTERVAL = 1000; // in milliseconds
    private static final int MIN_DISTANCE = 10; // in meters
    private static final String FINE_LOC_PERMISSION = "android.permission.ACCESS_FINE_LOCATION";

    private GeoCollector geoCollector;
    private OrientationCollector orientationCollector;
    private LightCollector lightCollector;
    private BatteryCollector batteryCollector;

    private UpdateSendor sendor;

    @Override
    public void onCreate() {
        super.onCreate();
        sendor = new UpdateSendor();
        LocalBroadcastManager.getInstance(this).registerReceiver(sendor,
                new IntentFilter("com.dell.iot.android.update"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting all collection services");
        startBatteryService();
        startLightService();
        startLocationService();
        startOrientationService();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBatteryService();
        stopLightService();
        stopLocationService();
        stopOrientationService();
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

}
