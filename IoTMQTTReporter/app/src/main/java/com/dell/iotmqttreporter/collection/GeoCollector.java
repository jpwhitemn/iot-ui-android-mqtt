package com.dell.iotmqttreporter.collection;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class GeoCollector implements LocationListener {

    private static final String TAG = "GeoChange";

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final float METER_TO_MILE = 2.23693629f;
    private static final float METER_TO_KM = 0.001f;
    private static final double METER_TO_FEET = 3.2808399;

    // required differences between current and last readings in order to send update
    private static final double COORD_CHG_DIFF = 0.1;
    private static final double ALT_CHG_DIFF = 100;
    private static final double SPD_CHG_DIFF = 10;

    private double[] lastFourAlts = {-1.0, -1.0, -1.0, -1.0};
    private LocationUoM uom = LocationUoM.English;

    private Location lastLocation;
    private double lastLat;
    private double lastLong;
    private double lastAltitude;
    private double lastSpeed;
    private Context ctx;

    public GeoCollector(Context ctx) {
        this.ctx = ctx;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        if (isBetterLocation(newLocation, lastLocation)) {
            HashMap<ReportKey, String> geoUpdateMap = new HashMap<>();
            updateLatLong(newLocation, geoUpdateMap);
            updateAltitude(newLocation, geoUpdateMap);
            updateSpeed(newLocation, geoUpdateMap);
            sendMessage(geoUpdateMap);
            lastLocation = newLocation;
        }
    }

    private void updateLatLong(Location newLocation, Map<ReportKey, String> geoUpdateMap) {
        if ((Math.abs(newLocation.getLatitude() - lastLat) > COORD_CHG_DIFF) ||
                (Math.abs(newLocation.getLongitude() - lastLong) > COORD_CHG_DIFF)) {
            Log.d(TAG, "Significant change in location detected: " + newLocation.getLatitude() + " " + newLocation.getLongitude());
            geoUpdateMap.put(ReportKey.latitude, Double.toString(newLocation.getLatitude()));
            geoUpdateMap.put(ReportKey.longitude, Double.toString(newLocation.getLongitude()));
            lastLat = newLocation.getLatitude();
            lastLong = newLocation.getLongitude();
            LastCollected.put(ReportKey.latitude, lastLat);
            LastCollected.put(ReportKey.longitude, lastLong);
        }
    }

    private void updateAltitude(Location newLocation, Map<ReportKey, String> geoUpdateMap) {
        if (newLocation.hasAltitude()) {
            double altitude = newLocation.getAltitude();
            if (uom.equals(LocationUoM.English)) {
                altitude = altitude * METER_TO_FEET;
            }
            altitude = smootheCurve(altitude);
            if (Math.abs(altitude - lastAltitude) > ALT_CHG_DIFF) {
                Log.d(TAG, "Significant altitude change detected:  " + altitude);
                geoUpdateMap.put(ReportKey.altitude, Double.toString(altitude));
                lastAltitude = altitude;
                LastCollected.put(ReportKey.altitude, lastAltitude);
            }
        }
    }

    private void updateSpeed(Location newLocation, Map<ReportKey, String> geoUpdateMap) {
        if (newLocation.hasSpeed()) {
            float myspeed = newLocation.getSpeed();
            if (uom.equals(LocationUoM.English)) {
                myspeed = myspeed * METER_TO_MILE;
            } else {
                myspeed = myspeed * METER_TO_KM;
            }
            if (Math.abs(myspeed - lastSpeed) > SPD_CHG_DIFF) {
                Log.d(TAG, "Significant speed change detected:  " + myspeed);
                geoUpdateMap.put(ReportKey.speed, Double.toString(myspeed));
                lastSpeed = myspeed;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * returns altitude off a smoothe curve so that results don't look odd
     */
    private double smootheCurve(double newAlt) {
        System.arraycopy(lastFourAlts, 0, lastFourAlts, 1, 3);
        lastFourAlts[0] = newAlt;
        double sum = 0.0f;
        int cnt = 0;
        for (double lastFourAlt : lastFourAlts) {
            if (lastFourAlt > 0) {
                sum = sum + lastFourAlt;
                cnt++;
            }
        }
        return sum / cnt;
    }

    private void sendMessage(HashMap<ReportKey, String> geoUpdateMap) {
        Intent updateIntent = new Intent();
        updateIntent.setAction("com.dell.iot.android.update");
        updateIntent.putExtra("updates", geoUpdateMap);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent);
    }
}
