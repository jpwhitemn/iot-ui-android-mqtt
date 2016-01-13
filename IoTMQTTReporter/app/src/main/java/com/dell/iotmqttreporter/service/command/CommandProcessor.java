/*******************************************************************************
 * Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.service.command;

import static com.dell.iotmqttreporter.service.command.CommandConstants.*;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dell.iotmqttreporter.collection.ReportKey;
import com.dell.iotmqttreporter.service.collection.CollectionService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by Jim on 1/10/2016.
 * <p/>
 * Responsible for processing the contents of each command message given to it by the CommandListener.
 * Determines if the command request is a GET or PUT command.
 * On PUT command, requests the collection service to stop collecting and sending device data.
 **/
public class CommandProcessor {

    private static final String TAG = "CommandProcessor";
    private final JsonParser parser = new JsonParser();
    private Context ctx;

    public CommandProcessor(Context ctx) {
        this.ctx = ctx;
    }

    @SuppressWarnings("ConstantConditions")
    public void process(byte[] payload) {
        try {
            Log.d(TAG, "Processing message:  " + new String(payload));
            String json = new String(payload);
            JsonObject jsonObject = parser.parse(json).getAsJsonObject();
            // get the command method (GET or PUT) from the MQTT command request message payload
            String method = extractCommandData(jsonObject, METHOD_KEY);
            // get the command message UUID from the payload.  This is needed in the response back to let the device service know how to pair the response to a request
            String uuid = extractCommandData(jsonObject, UUID_KEY);
            // if it is a get request, get the report key to know which last update to return in the response
            if (CMD_GET.equals(method)) {
                ReportKey key = ReportKey.valueOf(extractCommandData(jsonObject, CMD_KEY));
                Log.d(TAG, "Processing " + method + " command:  " + key.toString() + ", uuid: " + uuid);
                sendMessage(key, uuid, CMD_GET);
            } else { // its a put command request.  Request to turn off the collection service if the value is 0.  Turn on the collection service if the value is other than 0.  Return "ok"
                int onOffparam;
                try {
                    onOffparam = Integer.parseInt(extractCommand(jsonObject));
                } catch (NumberFormatException parseE) {
                    Log.i(TAG, "Request to turn on/off the collection sent a non-number value and was treated as zero (off).");
                    onOffparam = 0;
                }
                Log.d(TAG, "Processing " + method + " with parameters:  " + onOffparam + ", uuid: " + uuid);
                requestCollectionStartStop(onOffparam);
                sendMessage(ReportKey.name, uuid, CMD_PUT);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing command request:  " + new String(payload));
            e.printStackTrace();
        }
    }

    // get the data out of the MQTT message.  This includes the command request UUID, method (GET or PUT), report key (batterylevel, altitude, etc.), and parameter data.
    private String extractCommandData(JsonObject jsonObject, String dataPart) {
        if (jsonObject != null) {
            JsonElement element = jsonObject.get(dataPart);
            if (element != null)
                return element.getAsString();
        }
        return null;
    }

    // extract the parameter "Status" data
    private String extractCommand(JsonObject jsonObject) {
        JsonElement array = jsonObject.get(PARAMS_KEY);
        if (array != null) {
            Gson gson = new Gson();
            Command cmd = gson.fromJson(array, Command.class);
            if (CMD_PUT_NAME.equals(cmd.getName()))
                return cmd.getValue();
            return null;
        } else
            return null;
    }

    // send the response message via the CommandResponseSendor.  The CommandResponseSendor is a broadcast receiver.  So send an intent to trigger it to send.
    // The extra data in the message contains the key/value pairs to be sent in the body of the MQTT messsage
    private void sendMessage(ReportKey requestedKey, String uuid, String method) {
        Intent updateIntent = new Intent();
        updateIntent.setAction(CommandConstants.CMD_RESP_ACTION);
        updateIntent.putExtra(UUID_KEY, uuid);
        updateIntent.putExtra(METHOD_KEY, method);
        if (CMD_GET.equals(method))  // if a command GET request, include the report key so the sendor can get the lastest update
            updateIntent.putExtra(REPPORT_KEY, requestedKey.toString());
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent);
    }

    // send request to collection service to start or stop based on on command parameter
    private void requestCollectionStartStop(int on) {
        if (on != 0)
            startCollectionServices();
        else
            stopCollectionServices();
    }

    private void startCollectionServices() {
        Log.d(TAG, "Command request to start collecting rec'd.");
        Intent intent = new Intent(ctx, CollectionService.class);
        ctx.startService(intent);
    }

    private void stopCollectionServices() {
        Log.d(TAG, "Command request to stop collecting rec'd.");
        Intent intent = new Intent(ctx, CollectionService.class);
        ctx.stopService(intent);
    }

}
