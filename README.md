Main Author: Jim White

Language: Android v14 or better (Java 7)

This is an Android application that will send battery, light, geo info (altitude, speed, lat/long), and orientation (direction in degrees north) data via MQTT message to Fuse via the MQTT Device Service.

It was built with Android Studio 1.5.1

Copyright 2016, Dell, Inc.

# TODO - tasks that remain
- Provide MQTT Ping to keep the client/broker connection alive for the three queues.  The keep alive default for the app is currently 1 hour (3600 seconds) for all connections.
- add preferences mechanism (and corresponding data gathering converts) to switch UoM reporting data in English vs Metric units
- Clean up; astract out the commonalities between the CollectionUpdateSendor, CommandListener, and CommandResponseSendor
- Clean up; extract MQTT client and options builder work to a common class
- Add preferences (or some persistent mechanism) to save last readings so that these results can be sent on an initial data read or any command request when no value exists.

# MQTT component design
Here is a rough diagram of the components internal to the application that send/rec the varioius MQTT messages
