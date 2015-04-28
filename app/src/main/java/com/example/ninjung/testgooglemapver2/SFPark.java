package com.example.ninjung.testgooglemapver2;

/**
 * Created by ninjung on 4/20/15.
 */

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;




/**
 * Created by Steven on 4/19/2015.
 */

/**
 * Implements the SFPark API, allows users to retrieve parking information from the API.
 */
public class SFPark {

    private static String baseURL = "http://api.sfpark.org/sfpark/rest/availabilityservice?";
    private static String lati = "lat=";
    private static String longi = "long=";
    private static String restOfURL = "&radius=0.05&uom=mile&pricing=yes&response=json"; //changed from response = xml

    private static String jsonResult = "";

    /**
     * Networking operations are not allowed in the main thread of the application. This innerclass
     * allows for the execution of networking operations (HTTP Requests) on another thread.
     */
    static class FeedTask extends AsyncTask<Double, Void, String> {

        public AsyncResponse delegate = null;

        public FeedTask() {}

        public FeedTask (AsyncResponse resp)  {
            this.delegate = resp;
        }

        /**
         * This method can be executed in onCreate() and allows user to perform networking operations
         * on a separate thread.
         * @param coords Doubles representing Latitude and Longitude.
         * @return Parking information from SFPark with specified Latitude and Longitude.
         */
        protected String doInBackground(Double... coords) {
            String jsonData = "";

            try {
                URL url = new URL(baseURL + lati + coords[0] + "&" + longi + coords[1] + restOfURL);
                System.out.println(url);
                URLConnection urlConnection = url.openConnection();
                BufferedReader brIn = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;

                while ((line = brIn.readLine()) != null) {
                    jsonData += line;
                }

            } catch (MalformedURLException mURL) {
                System.out.println("EXCEPTION: Malformed URL!");
            } catch (IOException ioE) {
                System.out.println("EXCEPTION: I/O Exception! \"There was an error trying to open" +
                        " a connection!\"");
            }

            jsonResult = jsonData;
            return jsonData;
        }

        /**
         * Method that is executed right after doInBackground(). The result of doInBackground()
         * is automatically passed into this method as a parameter.
         * @param result The string that contains JSON data from doInBackground().
         */
        @Override
        protected void onPostExecute(String result) {

            jsonResult = result;

            Gson gson = new Gson();

            System.out.println("Parsing JSON...");
            SFP info = gson.fromJson(jsonResult, SFP.class);


            System.out.println("Printing the info from AVL...");

            //Accessing AVL ArrayList to get information from SFPark.
            if (info.getAVL().size() > 0) {
                System.out.println("The rates are : " + info.getAVL().get(0).getRATES());
                delegate.processFinish(info.getAVL());
            } else {
                System.out.println("No records were found!");
            }

        }

    }

}

/**
 * The main class used by GSON to convert JSON to Java objects.
 */
class SFP {
    private String STATUS;
    private String NUM_RECORDS;
    private String MESSAGE;

    private ArrayList<AVL> AVL = new ArrayList<AVL>();

    private static SFP instance = new SFP();

    private SFP() {
    }

    /**
     * Get the single instance of the object.
     * @return An instance of the SFP object.
     */
    public static SFP getInstance() {
        return instance;
    }

    /**
     * Get the STATUS of the JSON request.
     * @return Status of the JSON request.
     */
    public String getSTATUS() {
        return STATUS;
    }

    /**
     * Get the number of records returned by the SFPark API.
     * @return The number of records that were found.
     */
    public String getNUM_RECORDS() {
        return NUM_RECORDS;
    }

    /**
     * Get the message saying how many records have been found by the SFPark API.
     * @return String displaying how many records have been found.
     */
    public String getMESSAGE() {
        return MESSAGE;
    }

    /**
     * Get the ArrayList containing all the AVL objects from the SFPark API.
     * @return ArrayList that contain AVL object.
     */
    public ArrayList<AVL> getAVL() {
        return AVL;
    }

    @Override
    public String toString() {
        return "STATUS=" + STATUS + ", NUM_RECORDS=" + NUM_RECORDS + ", MESSAGE=" + MESSAGE;
    }
}

/**
 * The class used by GSON to represent the "AVL" array in JSON.
 */
class AVL {
    private String TYPE;
    private String BFID;
    private String NAME;
    private RATES RATES;
    private String PTS;
    private String LOC;

    /**
     * Get the type of parking (on street/off street) from SFPark.
     * @return String containing the type of parking.
     */
    public String getTYPE() {
        return TYPE;
    }

    /**
     * Get the BFID of the street from SFPark.
     * @return String containing BFID of the street.
     */
    public String getBFID() {
        return BFID;
    }

    /**
     * Get the name of the street from SFPark.
     * @return String containing the name of the street.
     */
    public String getNAME() {
        return NAME;
    }

    /**
     * Get the rates of the current request to the SFPark API.
     * @return A RATES object that contains the rate information.
     */
    public RATES getRATES() {
        return RATES;
    }

    @Override
    public String toString() {
        return "TYPE=" + TYPE + ", NAME=" + NAME;
    }
}

/**
 * The class used by GSON to represent the "RATES" object in JSON.
 */
class RATES {
    private ArrayList<RateInfo> RS = new ArrayList<RateInfo>();

    public ArrayList<RateInfo> getRS() {
        return RS;
    }

    public void setRS(ArrayList<RateInfo> rs) {
        this.RS = rs;
    }

    public String toString() {
        return RS.toString();
    }

}

/**
 * The class used by GSON to represent the "RS" array in JSON.
 */
class RateInfo {
    private String BEG;
    private String END;
    private String RATE;
    private String RQ;

    /**
     * Get the beginning time a parking meter requires money.
     * @return String containing the beginning time a meter requires money.
     */
    public String getBEG() {
        return BEG;
    }

    /**
     * Get the ending time a parking meter stops taking money.
     * @return String containing the ending time a meter stops taking money.
     */
    public String getEND() {
        return END;
    }

    /**
     * Get the rate of the parking meter.
     * @return String containing the rate of the parking meter.
     */
    public String getRATE() {
        return RATE;
    }

    /**
     * Get the rate qualifier of the parking meter.
     * @return String containing rate qualifier of the parking meter.
     */
    public String getRQ() {
        return RQ;
    }

    @Override
    public String toString() {
        return "Begin:" + BEG + " End:" + END + " Rate:" + RATE + " Rate Qualifier:" + RQ + "\n";
    }
}