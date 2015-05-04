package com.example.ninjung.testgooglemapver2;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;



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
    private static String restOfURL = "&radius=0.025&uom=mile&pricing=yes&response=json"; //changed from response = xml

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
         * @param coords: Doubles representing Latitude and Longitude.
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

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(RateInfo[].class, new RSDeserializer());
            Gson gson = gsonBuilder.create();

            System.out.println("Parsing JSON...");
            SFP info = gson.fromJson(jsonResult, SFP.class);


            System.out.println("Printing the info from AVL...");

            //Accessing AVL ArrayList to get information from SFPark.
            if (info.getAVL().size() > 0) {
                for (int i = 0; i < info.getAVL().size(); i++) {
                    System.out.println("The rates on " + info.getAVL().get(i).getNAME()
                            + " are : " + info.getAVL().get(i).getRATES());
                }

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

    /**
     * Get the status of the JSON request, the number of records found by the SFPark API and a
     * message containing the number of records.
     * @return String containing the status of the JSON request, the number of records found by
     * the SFPark API and a message containing the number of records.
     */
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

    /**
     * Get the type of parking and names of the street.
     * @return String containing the type of parking and name of street.
     */
    @Override
    public String toString() {
        return "TYPE=" + TYPE + ", NAME=" + NAME;
    }
}

/**
 * The class used by GSON to represent the "RATES" object in JSON.
 */
class RATES {
    private RateInfo[] RS;

    /**
     * Get all items on rate information.
     * @return Array of RateInfo objects containing data on rate information.
     */
    public RateInfo[] getRS() {
        return RS;
    }

    /**
     * Get a string of all of the data on rate information.
     * @return String containing all of the data on rate information.
     */
    public String toString() {
        String rates = "";
        for (int i = 0; i < RS.length; i++) {
            rates += RS[i].toString();
        }

        return rates;
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

    /**
     * Get the beginning and ending times, the rate and rate qualifier of a parking meter.
     * @return String containing the beginning and ending times, the rate and rate qualifier
     * of a parking meter.
     */
    @Override
    public String toString() {
        return "Begin:" + BEG + " End:" + END + " Rate:" + RATE + " Rate Qualifier:" + RQ + "\n";
    }
}

/**
 * Used for custom deserialization of "RS" attribute. "RS" attribute in JSON data can either contain
 * one object or an array of objects. This class detects that and properly deserializes the data.
 */
class RSDeserializer implements JsonDeserializer<RateInfo[]> {

    /**
     *
     * @param json: Instance of JSON element.
     * @param typeOfT: Type of JSON element.
     * @param context: Context of JSON object.
     * @return An array of RateInfo objects containing the deserialized information from the JSON data.
     * @throws JsonParseException
     */

    @Override
    public RateInfo[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException
    {
        if (json instanceof JsonArray) {
            return new Gson().fromJson(json, RateInfo[].class);
        }
        RateInfo rI = context.deserialize(json, RateInfo.class);

        return new RateInfo[] { rI };
    }

}