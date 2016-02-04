/*******************************************************************************
 * © Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.service.command;

/**
 * Created by Jim on 1/10/2016.
 *
 * This class holds a number of constant values and keys used in processing command requests.
 * The values should only be used in this package to simplify command sending.
 */
public class CommandConstants {
    // Intent broadcast (action) used to launch the CommandResponseSendor BroadcastReceiver
    public static final String CMD_RESP_ACTION = "com.dell.iot.android.commandresponse";

    // key to command request from MQTT Device Service.  The value will either be "collect" for put request or one of the ReportKeys for a get request.
    public static final String CMD_KEY = "cmd";
    // key to the command request type (either a get or put request)
    public static final String METHOD_KEY = "method";
    // key to reportkey value
    public static final String REPPORT_KEY = "reportkey";
    // key to the command request unique identifier for each request.
    public static final String UUID_KEY = "uuid";
    // key to the "collect" put request to turn on (true or false) the collecting service
    public static final String CMD_PUT_NAME = "collect";
    // key to the command put paramaters
    public static final String PARAMS_KEY = "params";

    // value of method indicator for get requests
    public static final String CMD_GET = "GET";
    // value of method indicator for put requests
    public static final String CMD_PUT = "PUT";

    // value of ping response
    public static final String PING_RESP = "pong";

    // key to preferences MQTT command response broker value
    public static final String RESPBROKER_KEY = "respbroker";
    // key to preferences MQTT command response client id value
    public static final String RESPCLIENTID_KEY = "respclient";
    // key to preferences MQTT command response user value
    public static final String RESPUSER_KEY = "respuser";
    // key to preferences MQTT command response password value
    public static final String RESPPASS_KEY = "resppass";
    // key to preferences MQTT command response topic value
    public static final String RESPTOPIC_KEY = "resptopic";

    // key to preferences MQTT incoming command request broker value
    public static final String INBROKER_KEY = "inbroker";
    // key to preferences MQTT incoming command request client id value
    public static final String INCLIENTID_KEY = "inclient";
    // key to preferences MQTT incoming command request user value
    public static final String INUSER_KEY = "inuser";
    // key to preferences MQTT incoming command request password value
    public static final String INPASS_KEY = "inpass";
    // key to preferences MQTT incoming command request topic value
    public static final String INTOPIC_KEY = "intopic";

    // key to MQTT response QOS
    public static final int QOS = 0;
    // key to MQTT response Keep Allive value (set to 1 hour)
    public static final int KEEP_ALIVE = 3600;


    // preferences key to the device name
    public static final String PREF_DEVICE_NAME = "devicename";

}
