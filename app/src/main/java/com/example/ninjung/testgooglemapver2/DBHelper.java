package com.example.ninjung.testgooglemapver2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

/**
 * Implements the class DBHelper which extends SQLiteOpenHelper using a singleton design pattern.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper dbHandler = null;

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "locations.db";
    private static final String TABLE_LOCATIONS = "locations";
    private static final String TABLE_PARKED = "parked";


    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_DATE = "date";
    public static final String MOST_RECENT_NUMBER = "5";

    /**
     * Private constructor for in class calls
     * @param context used to open or create database
     * @param name of the database or NULL for in-memory database
     * @param factory for creation of cursors or NULL for default
     * @param version version of database, increment when database structure is updated
     */
    private DBHelper(Context context, String name,
                    SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    /**
     * Method to return an instance of the DBHelper class. Used to only allow one instance of DBHelper
     * to run in order to prevent concurrent access.
     * @param context used to open or create an instance of DBHelper
     * @return the private member variable dbHandler, an instance of DBHelper.
     */
    public static DBHelper getInstance(Context context){
        if(dbHandler == null)
            dbHandler = new DBHelper(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);

        return dbHandler;
    }


    /**
     * Used to create tables first time app is run. Overrides onCreate
     * @param db database in which the tables need to be created in
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
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

    /**
     * Used to update tables or database structure when changes are made. Must increment database
     * version in order to be called.
     * @param db database in which the tables need to be updated
     * @param oldVersion old database version number
     * @param newVersion new database version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKED);

        onCreate(db);
    }

    /**
     * Used to drop all data in tables for testing purposes.
     */
    public void cleanDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKED);
        onCreate(db);
    }

    /**
     * adds a location to the location history database
     * @param location a LatLng object that holds a latitude/longitude in the form of 2 doubles
     */
    public void addLocation(LatLng location) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, location.latitude);
        values.put(COLUMN_LONGITUDE, location.longitude);
        values.put(COLUMN_DATE, System.currentTimeMillis());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_LOCATIONS, null, values);
        db.close();
    }

    /**
     * adds a location to the current parking state database
     * @param location a LatLng object that holds a latitude/longitude in the form of 2 doubles
     */
    public void addLocationParked(LatLng location) {
        ContentValues values2 = new ContentValues();
        values2.put(COLUMN_LATITUDE, location.latitude);
        values2.put(COLUMN_LONGITUDE, location.longitude);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_PARKED, null, values2);
        db.close();
    }

    /**
     * deletes a location in the locations history database by using a SQL query to organize the data
     * by the date added into the table. deletes the oldest entry.
     * @return true if the delete was successful, false otherwise.
     */
    public boolean deleteLocation() {
        boolean result = false;

        String query = "Select " + COLUMN_ID + " FROM " + TABLE_LOCATIONS + " ORDER BY "
                        + COLUMN_DATE + " ASC ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int id;

        if (cursor.moveToFirst()) {
            id = Integer.parseInt(cursor.getString(0));
            db.delete(TABLE_LOCATIONS, COLUMN_ID + " = " + id, null);
            cursor.close();
            result = true;
        }
        db.close();
        return result;
    }

    /**
     * deletes all data in the parked table when called
     */
    public void deleteLocationParked() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PARKED);

    }


    /**
     * takes an integer id which serves as a primary key search term for the history table. finds
     * the entry in the table who's id matches the given parameter.
     * @param id an integer search term used to search the table's primary keys
     * @return a LatLng location object that stores latitude/longitude in 2 doubles
     */
    public LatLng findLocation(int id) {
        String query = "Select * from " + TABLE_LOCATIONS + " WHERE " +
                COLUMN_ID + " = \"" + id + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        double latitude, longitude;
        LatLng location;

        if (cursor.moveToFirst()) {
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

    /**
     * Finds the first location in the parked table if it exists.
     * @return a LatLng object that holds a location in the form of 2 doubles
     */
    public LatLng findLocationParked() {
        String query = "Select * from " + TABLE_PARKED;

        SQLiteDatabase db = this.getWritableDatabase();


        Cursor cursor = db.rawQuery(query, null);

        double latitude, longitude;

        LatLng location;

        if (cursor.moveToFirst()) {
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


    /**
     * Counts all the entries in the parked table.
     * @return an integer referring to the number of entries in the table
     */
    public int getRowCountParked() {
        String query = "Select * FROM " + TABLE_PARKED;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();
    }

    /**
     * Counts all the entries in the locations table
     * @return an integer referring to the number of entries in the table
     */
    public int getRowCount() {
        String query = "Select * FROM " + TABLE_LOCATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();
    }

    /**
     * Retrieves the largest primary key ID from the locations table, indicating it is the entry
     * most recently added.
     * @return an integer that refers to the largest ID in the table or -1 if no entries are found
     */
    public int getNaxID() {
        String query = "Select MAX(" + COLUMN_ID + ") FROM " + TABLE_LOCATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
        else
            return -1;
    }

    /**
     * selects the last 5 entries loaded into the locations table and adds them into an arraylist
     * of LatLng objects
     * @return an arraylist of 5 latlng objects that hold the most recent entries added into the table
     */
    public ArrayList<LatLng> getRecentParking() {
        String query = "Select DISTINCT "+COLUMN_LATITUDE+", "+ COLUMN_LONGITUDE+
                " from " + TABLE_LOCATIONS +
                " Group by " +COLUMN_ID+
                " Order by "+COLUMN_ID + " desc "+
                " limit "+MOST_RECENT_NUMBER;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<LatLng> locations = new ArrayList<LatLng>();
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
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

    /**
     * Retrieves the parking spot in the saved state table if person is still parked.
     * @return a latlng object if there is an entry in the table. null if not.
     */
    public LatLng getLastParking() {
        String query = "select "+COLUMN_LATITUDE+", "+ COLUMN_LONGITUDE+
                " from " + TABLE_PARKED;
                //" where " +COLUMN_ID+
               // " =(select max("+COLUMN_ID+") from "+TABLE_LOCATIONS+")";


        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        LatLng location = null;
        if (cursor.moveToFirst()) {
                double latitude, longitude;
                latitude = Double.parseDouble(cursor.getString(0));
                longitude = Double.parseDouble(cursor.getString(1));
                location = new LatLng(latitude,longitude);
        }
        db.close();
        return location;
    }

}