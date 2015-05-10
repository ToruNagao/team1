package com.example.ninjung.testgooglemapver2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

public class DBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "locations.db";
    private static final String TABLE_LOCATIONS = "locations";
    private static final String TABLE_PARKED = "parked";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_DATE = "date";
    public static final String MOST_RECENT_NUMBER = "5";


    public DBHelper(Context context, String name,
                    SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_TABLE_LOCATIONS = "CREATE TABLE " + TABLE_LOCATIONS
                + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + COLUMN_LATITUDE + " DOUBLE ,"
                + COLUMN_LONGITUDE + " DOUBLE ,"
                + COLUMN_DATE + " INTEGER) ";

        String CREATE_TABLE_PARKED = "CREATE TABLE " + TABLE_PARKED
                + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + COLUMN_LATITUDE + " DOUBLE ,"
                + COLUMN_LONGITUDE + " DOUBLE) ";

        db.execSQL(CREATE_TABLE_LOCATIONS);
        db.execSQL(CREATE_TABLE_PARKED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);

        onCreate(db);
    }

    public void cleanDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKED);
        onCreate(db);
    }

    public void addLocation(LatLng location){
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, location.latitude);
        values.put(COLUMN_LONGITUDE, location.longitude);
        values.put(COLUMN_DATE, System.currentTimeMillis());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_LOCATIONS, null, values);
        db.close();
    }

    public void addLocationParked(LatLng location){
        ContentValues values2 = new ContentValues();
        values2.put(COLUMN_LATITUDE, location.latitude);
        values2.put(COLUMN_LONGITUDE, location.longitude);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_PARKED, null, values2);
        db.close();
    }


    public boolean deleteLocation(){
        boolean result = false;

        String query = "Select " + COLUMN_ID + " FROM " + TABLE_LOCATIONS + " ORDER BY "
                        + COLUMN_DATE + " ASC ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int id;

        if(cursor.moveToFirst()){
            id = Integer.parseInt(cursor.getString(0));
            db.delete(TABLE_LOCATIONS, COLUMN_ID + " = " + id, null);
            cursor.close();
            result = true;
        }
        db.close();
        return result;
    }

    public void deleteLocationParked(){

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PARKED);

    }




    public LatLng findLocation(int id){
        String query = "Select * from " + TABLE_LOCATIONS + " WHERE " +
                COLUMN_ID + " = \"" + id + "\"";

        SQLiteDatabase db = this.getWritableDatabase();


        Cursor cursor = db.rawQuery(query, null);

        double latitude, longitude;

        LatLng location;

        if(cursor.moveToFirst()){
            cursor.moveToFirst();

            latitude = Double.parseDouble(cursor.getString(1));
            longitude = Double.parseDouble(cursor.getString(2));
            cursor.close();

            location = new LatLng(latitude, longitude);

        }
        else
            location = null;

        db.close();
        return location;
    }

    public LatLng findLocationParked(){
        String query = "Select * from " + TABLE_PARKED;

        SQLiteDatabase db = this.getWritableDatabase();


        Cursor cursor = db.rawQuery(query, null);

        double latitude, longitude;

        LatLng location;

        if(cursor.moveToFirst()){
            cursor.moveToFirst();

            latitude = Double.parseDouble(cursor.getString(1));
            longitude = Double.parseDouble(cursor.getString(2));
            cursor.close();

            location = new LatLng(latitude, longitude);

        }
        else
            location = null;

        db.close();
        return location;
    }


    public int getRowCountParked(){
        String query = "Select * FROM " + TABLE_PARKED;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();
    }


    public int getRowCount(){
        String query = "Select * FROM " + TABLE_LOCATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();
    }

    public int getNaxID(){
        String query = "Select MAX(" + COLUMN_ID + ") FROM " + TABLE_LOCATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
        else
            return -1;
    }

    public ArrayList<LatLng> getRecentParking(){
        String query = "Select DISTINCT "+COLUMN_LATITUDE+", "+ COLUMN_LONGITUDE+
                " from " + TABLE_LOCATIONS +
                " Group by " +COLUMN_ID+
                " Order by "+COLUMN_ID + " desc "+
                " limit "+MOST_RECENT_NUMBER;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<LatLng> locations = new ArrayList<LatLng>();
        if(cursor.moveToFirst()){
            while(cursor.isAfterLast() == false) {
                LatLng location;
                double latitude, longitude;
                latitude = Double.parseDouble(cursor.getString(0));
                longitude = Double.parseDouble(cursor.getString(1));
                location = new LatLng(latitude, longitude);
                locations.add(location);
                cursor.moveToNext();
            }
        }
        else
            locations = null;

        db.close();
        return locations;
    }

    public LatLng getLastestParking(){
        String query = "select "+COLUMN_LATITUDE+", "+ COLUMN_LONGITUDE+
                " from " + TABLE_LOCATIONS +
                " where " +COLUMN_ID+
                " =(select max("+COLUMN_ID+") from "+TABLE_LOCATIONS+")";


        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        LatLng location = null;
        if(cursor.moveToFirst()){
                double latitude, longitude;
                latitude = Double.parseDouble(cursor.getString(0));
                longitude = Double.parseDouble(cursor.getString(1));
                location = new LatLng(latitude,longitude);
        }
        db.close();
        return location;
    }

}