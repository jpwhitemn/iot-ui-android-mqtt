/*******************************************************************************
 * © Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.collection;

/**
 * Created by Jim on 1/10/2016.
 * <p/>
 * Defines the keys for collected data by this device.  The name and UUID are additional values that are reported by the device back to the MQTT device service.
 */
public enum ReportKey {
    batterylevel, batteryhealth, batteryvoltage, batterytemperature, direction, altitude, latitude, longitude, speed, lightlevel, name, uuid
}
