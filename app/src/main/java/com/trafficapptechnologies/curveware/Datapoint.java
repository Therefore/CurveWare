package com.trafficapptechnologies.curveware;

import android.provider.ContactsContract;

import com.google.android.gms.maps.model.LatLng;

public class Datapoint {

    private static int uid;
    private static double latitude;
    private static double longitude;
    private static double distance;
    private static float speed;
    private static long duration;
    private static double inclination;
    private static String curveDirection;


    private static int counter = 0;

    public static Datapoint createDatapoint(int uid, double latitude, double longitude, double distance, float speed, long duration, double inclination, String curveDirection){
        return new Datapoint(uid, latitude,longitude,distance,speed,duration,inclination,curveDirection);
    }

    public Datapoint(int uid, double latitude, double longitude, double distance, float speed, long duration, double inclination, String curveDirection) {
        counter++;
        this.uid = uid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.speed = speed;
        this.duration = duration;
        this.inclination = inclination;
        this.curveDirection = curveDirection;
    }

    public static int getUid() {
        return uid;
    }

    public static void setUid(int uid) {
        Datapoint.uid = uid;
    }

    public static double getLatitude() {
        return latitude;
    }

    public static void setLatitude(double latitude) {
        Datapoint.latitude = latitude;
    }

    public static double getLongitude() {
        return longitude;
    }

    public static void setLongitude(double longitude) {
        Datapoint.longitude = longitude;
    }

    public static double getDistance() {
        return distance;
    }

    public static void setDistance(double distance) {
        Datapoint.distance = distance;
    }

    public static float getSpeed() {
        return speed;
    }

    public static void setSpeed(float speed) {
        Datapoint.speed = speed;
    }

    public static double getInclination() {
        return inclination;
    }

    public static void setInclination(double inclination) {
        Datapoint.inclination = inclination;
    }

    public static String getCurveDirection() {
        return curveDirection;
    }

    public static void setCurveDirection(long duration) {
        Datapoint.curveDirection = curveDirection;
    }

    public static long getDuration() {
        return duration;
    }

    public static void setDuration(long duration) {
        Datapoint.duration = duration;
    }

    public static int getCounter() {
        return counter;
    }

    @Override
    public String toString() {
        return "Datapoint{" +
                "uid=" + uid +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", distance=" + distance +
                ", speed=" + speed +
                ", inclination=" + inclination +
                ", curveDirection='" + curveDirection + '\'' +
                '}';
    }
}
