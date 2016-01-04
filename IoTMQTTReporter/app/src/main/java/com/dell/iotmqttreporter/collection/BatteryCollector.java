package com.dell.iotmqttreporter.collection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;

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
        HashMap<ReportLevel, String> batteryUpdateMap = new HashMap<>();

        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        if (last_health != health) {
            Log.d(TAG, "Battery health change detected:  " + health);
            batteryUpdateMap.put(ReportLevel.batteryhealth, Integer.toString(health));
            last_health = health;
        }
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        if (Math.abs(last_level - level) > LVL_CHG_DIFF) {
            Log.d(TAG, "Significant battery level change detected:  " + level);
            batteryUpdateMap.put(ReportLevel.batterylevel, Integer.toString(level));
            last_level = level;
        }
        if (Math.abs(last_temperature - temperature) > TEMP_CHG_DIFF) {
            Log.d(TAG, "Significant battery temperature change detected:  " + temperature);
            batteryUpdateMap.put(ReportLevel.batterytemperature, Integer.toString(temperature));
            last_temperature = temperature;
        }
        if (Math.abs(last_voltage - voltage) > VOLT_CHG_DIFF) {
            Log.d(TAG, "Significant battery voltage change detected:  " + voltage);
            batteryUpdateMap.put(ReportLevel.batteryvoltage, Integer.toString(voltage));
            last_voltage = voltage;
        }
        sendMessage(context, batteryUpdateMap);
    }

    public int getLast_health() {
        HashMap<ReportLevel, String> batteryUpdateMap = new HashMap<>();
        batteryUpdateMap.put(ReportLevel.batteryhealth, Integer.toString(health));
        return last_health;
    }

    public int getLast_level() {
        return last_level;
    }

    public int getLast_temperature() {
        return last_temperature;
    }

    public int getLast_voltage() {
        return last_voltage;
    }

    private void sendMessage(Context ctx, HashMap<ReportLevel, String> batteryUpdateMap){
        Intent updateIntent = new Intent();
        updateIntent.setAction("com.dell.iot.android.update");
        updateIntent.putExtra("updates", batteryUpdateMap);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent);
    }

}
