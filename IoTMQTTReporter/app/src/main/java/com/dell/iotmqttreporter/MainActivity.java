package com.dell.iotmqttreporter;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.dell.iotmqttreporter.collection.ReportLevel;
import com.dell.iotmqttreporter.service.CollectionService;
import com.dell.iotmqttreporter.service.CommandListener;
import com.dell.iotmqttreporter.service.CommandService;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView deviceNameTV, batteryHealthTV, batteryLevelTV, batteryTempTV, batteryVoltTV, latTV, longTV, altTV, speedTV, lightLevelTV, orientationTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deviceNameTV = (TextView) findViewById(R.id.deviceNameTV);
        batteryHealthTV = (TextView) findViewById(R.id.batteryHealthTV);
        batteryLevelTV = (TextView) findViewById(R.id.batteryLevelTV);
        batteryTempTV = (TextView) findViewById(R.id.batteryTempTV);
        batteryVoltTV = (TextView) findViewById(R.id.batteryVoltTV);
        latTV = (TextView) findViewById(R.id.latTV);
        longTV = (TextView) findViewById(R.id.longTV);
        altTV = (TextView) findViewById(R.id.altTV);
        speedTV = (TextView) findViewById(R.id.speedTV);
        lightLevelTV = (TextView) findViewById(R.id.lightLevelTV);
        orientationTV = (TextView) findViewById(R.id.orientationTV);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        String deviceName = prefs.getString("devicename", null);
        deviceNameTV.setText(deviceName);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register to receive local broadcast messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(updateMessageReceiver,
                new IntentFilter("com.dell.iot.android.update"));
        //TODO - restore latest readings
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateMessageReceiver);
        // TODO - store latest readings
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, MsgPreferencesActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_start) {
            startCollectionServices();
        } else if (id == R.id.action_stop) {
            stopCollectionServices();
        } else if (id == R.id.action_listen) {
            startListeningServices();
        } else if (id == R.id.action_mute) {
            stopLiseteningServices();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // adding and removing menu items creates problems, so best to just hide them.
        if (isServiceRunning(CollectionService.class)) {
            menu.findItem(R.id.action_start).setVisible(false);
            menu.findItem(R.id.action_stop).setVisible(true);
        } else {
            menu.findItem(R.id.action_start).setVisible(true);
            menu.findItem(R.id.action_stop).setVisible(false);
        }
        if (isServiceRunning(CommandService.class)) {
            menu.findItem(R.id.action_listen).setVisible(false);
            menu.findItem(R.id.action_mute).setVisible(true);
        } else {
            menu.findItem(R.id.action_listen).setVisible(true);
            menu.findItem(R.id.action_mute).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String deviceName = prefs.getString("devicename", null);
        deviceNameTV.setText(deviceName);
    }

    public void updateLevels(Map<ReportLevel, String> levels) {
        for (ReportLevel level : levels.keySet()) {
            switch (level) {
                case batterylevel:
                    batteryLevelTV.setText(levels.get(ReportLevel.batterylevel));
                    break;
                case batteryhealth:
                    batteryHealthTV.setText(levels.get(ReportLevel.batteryhealth));
                    break;
                case batterytemperature:
                    batteryTempTV.setText(levels.get(ReportLevel.batterytemperature));
                    break;
                case batteryvoltage:
                    batteryVoltTV.setText(levels.get(ReportLevel.batteryvoltage));
                    break;
                case altitude:
                    altTV.setText(levels.get(ReportLevel.altitude));
                    break;
                case latitude:
                    latTV.setText(levels.get(ReportLevel.latitude));
                    break;
                case longitude:
                    longTV.setText(levels.get(ReportLevel.longitude));
                    break;
                case speed:
                    speedTV.setText(levels.get(ReportLevel.speed));
                    break;
                case direction:
                    orientationTV.setText(levels.get(ReportLevel.direction));
                    break;
                case lightlevel:
                    lightLevelTV.setText(levels.get(ReportLevel.lightlevel));
                    break;
            }
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startCollectionServices() {
        Intent intent = new Intent(this, CollectionService.class);
        startService(intent);
    }

    private void startListeningServices() {
        Intent intent = new Intent(this, CommandService.class);
        startService(intent);
    }

    private void stopCollectionServices() {
        Intent intent = new Intent(this, CollectionService.class);
        stopService(intent);
    }

    private void stopLiseteningServices() {
        Intent intent = new Intent(this, CommandService.class);
        stopService(intent);
    }

    // handler for received collector "com.dell.iot.android.update" events
    private BroadcastReceiver updateMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HashMap<ReportLevel, String> updateMap = (HashMap) intent.getSerializableExtra("updates");
            if (updateMap.size() > 0)
                updateLevels(updateMap);
        }
    };
}