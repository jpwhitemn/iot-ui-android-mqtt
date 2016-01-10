/*******************************************************************************
 * © Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.service.command;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Jim on 1/10/2016.
 *
 * Background service started by the selection of "Start Command Listening" from the menu on the main screen activity.
 * <p/>
 * Starts the CommandResponseSendor (a Broadcast Receiver) when it is created.
 * It starts the CommandListener (an MQTT Callback) when the service started.
 * The command listener listens for new MQTT messages coming on the AndroidDSTopic.
 * The command response sendor, sends response messages back out on the CmdRespTopic.
 * <p/>
 * It stops both the listener and sendor when it is destroyed.
 **/
public class CommandService extends Service {

    private static final String TAG = "CommandService";

    private CommandListener listener;
    private CommandResponseSendor sendor;

    @Override
    public void onCreate() {
        super.onCreate();
        // start the BroadcastReceiver responsible for sending command response messages via MQTT
        sendor = new CommandResponseSendor();
        LocalBroadcastManager.getInstance(this).registerReceiver(sendor,
                new IntentFilter(CommandConstants.CMD_RESP_ACTION));
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // stop the listening for new MQTT messages (via the callback)
        listener.cleanup();
        // stop the broadcast receiver responsible for sending command response messages
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sendor);
        Log.d(TAG, "Stopped the command service.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting the command service.");
        // start the listening for new MQTT command messages (via MQTT callback)
        listener = new CommandListener(this.getApplicationContext());
        listener.startListening();
        return super.onStartCommand(intent, flags, startId);
    }
}
