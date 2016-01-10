/*******************************************************************************
 * Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jim on 1/10/2016.
 * <p/>
 * Singleton that stores the latest readings captured by the device.
 * Data is stored by ReportKey in a Map.
 * Data (value) could be of any type - therefore the map is to an "Object".
 */
public class LastCollected {

    private static LastCollected instance = null;

    private Map<ReportKey, Object> lastUpdates = new HashMap<>();

    protected LastCollected() {
    }

    public static LastCollected getInstance() {
        if (instance == null)
            instance = new LastCollected();
        return instance;
    }

    public Map<ReportKey, Object> getLastUpdates() {
        return lastUpdates;
    }

    public void setLastUpdates(Map<ReportKey, Object> lastUpdates) {
        this.lastUpdates = lastUpdates;
    }

    public static void put(ReportKey key, Object value) {
        getInstance().lastUpdates.put(key, value);
    }

    public static Object get(ReportKey key) {
        return getInstance().lastUpdates.get(key);
    }

}
