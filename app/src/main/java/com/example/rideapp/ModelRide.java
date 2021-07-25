package com.example.rideapp;

public class ModelRide {
    private String topSpeed, distance, time, timestamp;

    public ModelRide(){

    }

    public ModelRide(String distance, String time, String topSpeed, String timestamp){
        this.distance = distance;
        this.time = time;
        this.topSpeed = topSpeed;
        this.timestamp = timestamp;
    }

    public String getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(String topSpeed) {
        this.topSpeed = topSpeed;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimestamp(){
        return timestamp;
    }
}
