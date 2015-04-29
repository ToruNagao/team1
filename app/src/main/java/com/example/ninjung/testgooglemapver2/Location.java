package com.example.ninjung.testgooglemapver2;

/**
 * Created by aznanimekidMSI on 4/28/2015.
 */
public class Location {

    private int _id;
    private double _latitude;
    private double _longitude;

    public Location(){

    }

    public Location(int id, double latitude, double longitude){
        this._id = id;
        this._latitude = latitude;
        this._longitude = longitude;
    }

    public Location(double latitude, double longitude){
        this._latitude = latitude;
        this._longitude = longitude;
    }

    public void setID(int id){
        this._id = id;
    }

    public void setLatitude(double latitude){
        this._latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this._longitude = longitude;
    }

    public int getID(){
        return this._id;
    }

    public double getLatitude(){
        return this._latitude;
    }

    public double getLongitude(){
        return this._longitude;
    }
}
