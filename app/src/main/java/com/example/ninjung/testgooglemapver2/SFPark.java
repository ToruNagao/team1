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

import org.xml.sax.helpers.DefaultHandler;


/**
 * Created by Steven on 4/19/2015.
 */

/**
 * Implements the SFPark API, allows users to retrieve parking information from the API.
 */
public class SFPark extends DefaultHandler {

    private static String baseURL = "http://api.sfpark.org/sfpark/rest/availabilityservice?";
    private static String lati = "lat=";
    private static String longi = "long=";
    private static String restOfURL = "&radius=0.05&uom=mile&pricing=yes&response=json"; //changed from response = xml

    private static String xmlResult = "";
    private static String jsonResult = "";

    /**
     * Networking operations are not allowed in the main thread of the application. This innerclass
     * allows for the execution of networking operations (HTTP Requests) on another thread.
     */
    static class FeedTask extends AsyncTask<Double, Void, String> {

        /**
         * This method can be executed in onCreate() and allows user to perform networking operations
         * on a separate thread.
         *
         * @param coords Doubles representing Latitude and Longitude.
         * @return Parking information from SFPark with specified Latitude and Longitude.
         */
        protected String doInBackground(Double... coords) {
            String xmlData = "";
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

        @Override
        protected void onPostExecute(String result) {
            System.out.println("************PRINTING JSON DATA*****************");
            System.out.println(result);
            System.out.println("Finished Printing JSON data");
            //xmlResult = result;
            jsonResult = result;

            Gson gson = new Gson();

            System.out.println("Parsing JSON...");
            SFP info = gson.fromJson(jsonResult, SFP.class);

            System.out.println("Printing the info from AVL...");
            if (info.getAVL().size() > 0) {
                System.out.println("The rates are : " + info.getAVL().get(0).getRATES());
            } else {
                System.out.println("No records were found!");
            }

        }

    }

}

class SFP {
    private String STATUS;
    private String NUM_RECORDS;
    private String MESSAGE;

    private ArrayList<AVL> AVL = new ArrayList<AVL>();

    private static SFP instance = new SFP();

    private SFP() {
    }

    public static SFP getInstance() {
        return instance;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String status) {
        STATUS = status;
    }

    public String getNUM_RECORDS() {
        return NUM_RECORDS;
    }

    public void setNUM_RECORDS(String numOfRecords) {
        NUM_RECORDS = numOfRecords;
    }

    public String getMESSAGE() {
        return MESSAGE;
    }

    public void setMESSAGE(String msg) {

        MESSAGE = msg;
    }

    public ArrayList<AVL> getAVL() {
        return AVL;
    }

    public void setAVL(ArrayList<AVL> avl) {
        AVL = avl;
    }

    public void testPrint() {
        System.out.println("The size of AVL is " + AVL.size());

        if (AVL.size() > 0) {
            for (int i = 0; i < AVL.size(); i++) {
                System.out.println("PRINTING AVL[" + i + "]" + AVL.get(i));
            }
        }
    }

    public String toString() {
        return "STATUS=" + STATUS + ", NUM_RECORDS=" + NUM_RECORDS + ", MESSAGE=" + MESSAGE;
    }
}

class AVL {
    private String TYPE;
    private String BFID;
    private String NAME;
    private RATES RATES;
    private String PTS;
    private String LOC;

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String type) {
        TYPE = type;
    }

    public String getBFID() {
        return BFID;
    }

    public void setBFID(String id) {
        BFID = id;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String name) {
        NAME = name;
    }

    public RATES getRATES() {
        return RATES;
    }

    public void setRATES(RATES rateInfo) {
        RATES = rateInfo;
    }

    public String getPTS() {
        return PTS;
    }

    public void setPTS(String pts) {
        this.PTS = pts;
    }

    public String getLOC() {
        return LOC;
    }

    public void setLOC(String loc) {
        this.LOC = loc;
    }

    public String toString() {
        return "TYPE=" + TYPE + ", NAME=" + NAME;
    }
}

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

class RateInfo {
    private String BEG;
    private String END;
    private String RATE;
    private String RQ;

    public String getBEG() {
        return BEG;
    }

    public void setBEG(String BEG) {
        this.BEG = BEG;
    }

    public String getEND() {
        return END;
    }

    public void setEND(String END) {
        this.END = END;
    }

    public String getRATE() {
        return RATE;
    }

    public void setRATE(String RATE) {
        this.RATE = RATE;
    }

    public String getRQ() {
        return RQ;
    }

    public void setRQ(String RQ) {
        this.RQ = RQ;
    }

    public String toString() {
        return "Begin:" + BEG + " End:" + END + " Rate:" + RATE;
    }
}

