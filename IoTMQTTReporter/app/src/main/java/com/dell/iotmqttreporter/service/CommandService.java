package com.dell.iotmqttreporter.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class CommandService extends Service {

    private static final String TAG = "CommandService";
    private static final String CMD_RESP_ACTION = "com.dell.iot.android.commandresponse";


    private CommandListener listener;
    private CommandResponseSendor sendor;

    @Override
    public void onCreate() {
        super.onCreate();
        sendor = new CommandResponseSendor();
        LocalBroadcastManager.getInstance(this).registerReceiver(sendor,
                new IntentFilter(CMD_RESP_ACTION));
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener.cleanup();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sendor);
        Log.d(TAG, "Stopped the command service.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting the command service.");
        listener = new CommandListener(this.getApplicationContext());
        listener.startListening();
        return super.onStartCommand(intent, flags, startId);
    }
}
