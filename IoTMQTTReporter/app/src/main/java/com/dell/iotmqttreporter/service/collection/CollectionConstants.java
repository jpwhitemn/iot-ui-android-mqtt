/*******************************************************************************
 * © Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.service.collection;

/**
 * Created by Jim on 1/10/2016.
 * <p/>
 * This class holds a number of constant values and keys used in sending of collected device data.
 * The values should only be used in this package and collector package to simplify data sending.
 */
public class CollectionConstants {

    public static final int LOCATION_INTERVAL = 1000; // in milliseconds
    public static final int MIN_DISTANCE = 10; // in meters
    public static final String FINE_LOC_PERMISSION = "android.permission.ACCESS_FINE_LOCATION";

    // Intent action/key used to trigger CollectionUpdateSendor
    public static final String UPDATE_COLLECTION_ACTION ="com.dell.iot.android.update";

    // keys to preferences values to get the MQTT connection to the MQTT device service data topic
    public static final String OUTBROKER_KEY = "outbroker";
    public static final String OUTCLIENTID_KEY = "outclient";
    public static final String OUTUSER_KEY = "outuser";
    public static final String OUTPASS_KEY = "outpass";
    public static final String OUTTOPIC_KEY = "outtopic";
    public static final int QOS = 0;
    // MQTT keep alive set to 1 hour
    public static final int KEEP_ALIVE = 3600;

    // key to the update data stored in the Intent triggering the message send
    public static final String INTENT_UPD_KEY = "updates";

    // preferences key to the device name
    public static final String PREF_DEVICE_NAME = "devicename";
    // preference key to the incoming

}
