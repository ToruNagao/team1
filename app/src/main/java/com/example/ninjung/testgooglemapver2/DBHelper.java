package com.example.ninjung.testgooglemapver2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "locations.db";
    private static final String TABLE_LOCATIONS = "locations";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
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
                + COLUMN_LONGITUDE + " DOUBLE)";

        db.execSQL(CREATE_TABLE_LOCATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);

        onCreate(db);
    }

    public void cleanDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        onCreate(db);
    }

    public void addLocation(Location location){
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_LONGITUDE, location.getLongitude());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_LOCATIONS, null, values);
        db.close();
    }

    public Location findLocation(double latitude){
        String query = "Select * from " + TABLE_LOCATIONS + " WHERE " +
                        COLUMN_LATITUDE + " = \"" + latitude + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        Location location = new Location();

        if(cursor.moveToFirst()){
            cursor.moveToFirst();
            location.setID(Integer.parseInt(cursor.getString(0)));
            location.setLatitude(Double.parseDouble(cursor.getString(1)));
            location.setLongitude(Double.parseDouble(cursor.getString(2)));
            cursor.close();
        }
        else
            location = null;

        db.close();
        return location;
    }

    public Location findLocation(int id){
        String query = "Select * from " + TABLE_LOCATIONS + " WHERE " +
                COLUMN_ID + " = \"" + id + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        Location location = new Location();

        if(cursor.moveToFirst()){
            cursor.moveToFirst();
            location.setID(Integer.parseInt(cursor.getString(0)));
            location.setLatitude(Double.parseDouble(cursor.getString(1)));
            location.setLongitude(Double.parseDouble(cursor.getString(2)));
            cursor.close();
        }
        else
            location = null;

        db.close();
        return location;
    }

    public boolean deleteLocation(double latitude, double longitude){
        boolean result = false;

        String query = "Select * FROM " + TABLE_LOCATIONS + " WHERE " + COLUMN_LATITUDE +
                        " = \"" + latitude + "\" AND " + COLUMN_LONGITUDE + " = \"" + longitude + "\"";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Location location = new Location();

        if(cursor.moveToFirst()){
            location.setID(Integer.parseInt(cursor.getString(0)));
            db.delete(TABLE_LOCATIONS, COLUMN_ID + " = ? ",
                    new String[] { String.valueOf(location.getID()) });
            cursor.close();
            result = true;
        }
        db.close();
        return result;
    }

    public int getRowCount(){
        String query = "Select * FROM " + TABLE_LOCATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();

    }
    public ArrayList<Location> getRecentParking(){
        String query = "Select DISTINCT "+COLUMN_LATITUDE+", "+ COLUMN_LONGITUDE+
                " from " + TABLE_LOCATIONS +
                " Group by " +COLUMN_ID+
                " Order by "+COLUMN_ID + " desc "+
                " limit "+MOST_RECENT_NUMBER;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        ArrayList<Location> locations = new ArrayList<Location>();
        if(cursor.moveToFirst()){
            while(cursor.isAfterLast() == false) {
                Location location = new Location();
                location.setLatitude(Double.parseDouble(cursor.getString(0)));
                location.setLongitude(Double.parseDouble(cursor.getString(1)));
                locations.add(location);
                cursor.moveToNext();
            }
        }
        else
            locations = null;

        db.close();
        return locations;
    }
}
