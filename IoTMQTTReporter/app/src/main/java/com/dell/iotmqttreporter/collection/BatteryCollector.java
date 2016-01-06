package com.dell.iotmqttreporter.collection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class BatteryCollector extends BroadcastReceiver {

    private static final String TAG = "BatteryChange";

    // required differences between current and last readings in order to send update
    private static final int LVL_CHG_DIFF = 5;
    private static final int TEMP_CHG_DIFF = 5;
    private static final int VOLT_CHG_DIFF = 10;

    private int last_health;
    private int last_level;
    private int last_temperature;
    private int last_voltage;

    @Override
    public void onReceive(Context context, Intent intent) {
        HashMap<ReportKey, String> batteryUpdateMap = new HashMap<>();
        updateBatteryHealth(intent, batteryUpdateMap);
        updateBatteryLevel(intent, batteryUpdateMap);
        updateBatteryTemperature(intent, batteryUpdateMap);
        updateBatteryVoltage(intent, batteryUpdateMap);
        sendMessage(context, batteryUpdateMap);
    }

    private void updateBatteryHealth(Intent intent, Map<ReportKey, String> batteryUpdateMap) {
        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        if (last_health != health) {
            Log.d(TAG, "Battery health change detected:  " + health);
            batteryUpdateMap.put(ReportKey.batteryhealth, Integer.toString(health));
            last_health = health;
            LastCollected.put(ReportKey.batteryhealth, last_health);
        }
    }

    private void updateBatteryLevel(Intent intent, Map<ReportKey, String> batteryUpdateMap) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        if (Math.abs(last_level - level) > LVL_CHG_DIFF) {
            Log.d(TAG, "Significant battery level change detected:  " + level);
            batteryUpdateMap.put(ReportKey.batterylevel, Integer.toString(level));
            last_level = level;
            LastCollected.put(ReportKey.batterylevel, last_level);
        }
    }

    private void updateBatteryTemperature(Intent intent, Map<ReportKey, String> batteryUpdateMap) {
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        if (Math.abs(last_temperature - temperature) > TEMP_CHG_DIFF) {
            Log.d(TAG, "Significant battery temperature change detected:  " + temperature);
            batteryUpdateMap.put(ReportKey.batterytemperature, Integer.toString(temperature));
            last_temperature = temperature;
            LastCollected.put(ReportKey.batterytemperature, last_temperature);
        }
    }

    private void updateBatteryVoltage(Intent intent, Map<ReportKey, String> batteryUpdateMap) {
        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        if (Math.abs(last_voltage - voltage) > VOLT_CHG_DIFF) {
            Log.d(TAG, "Significant battery voltage change detected:  " + voltage);
            batteryUpdateMap.put(ReportKey.batteryvoltage, Integer.toString(voltage));
            last_voltage = voltage;
            LastCollected.put(ReportKey.batteryvoltage, last_voltage);
        }
    }

    private void sendMessage(Context ctx, HashMap<ReportKey, String> batteryUpdateMap) {
        Intent updateIntent = new Intent();
        updateIntent.setAction("com.dell.iot.android.update");
        updateIntent.putExtra("updates", batteryUpdateMap);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent);
    }
}
