package com.dell.iotmqttreporter.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CommandService extends Service {

    private static final String TAG = "CommandService";

    private CommandListener listener;

    public CommandService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener.cleanup();
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
