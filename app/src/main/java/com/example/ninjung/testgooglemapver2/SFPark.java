package com.example.ninjung.testgooglemapver2;

/**
 * Created by ninjung on 4/20/15.
 */

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
    private static String restOfURL = "&radius=0.05&uom=mile&pricing=yes&response=xml";

    private static String xmlResult = "";

    public static String getXmlResult() {
        return xmlResult;
    }

    /**
     * Networking operations are not allowed in the main thread of the application. This innerclass
     * allows for the execution of networking operations (HTTP Requests) on another thread.
     */
    static class FeedTask extends AsyncTask<Double, Void, String> {

        /**
         * This method can be executed in onCreate() and allows user to perform networking operations
         * on a separate thread.
         * @param coords Doubles representing Latitude and Longitude.
         * @return Parking information from SFPark with specified Latitude and Longitude.
         */
        protected String doInBackground(Double... coords) {
            String xmlData = "";

            try {
                URL url = new URL(baseURL + lati + coords[0] + "&" + longi + coords[1] + restOfURL);
                URLConnection urlConnection = url.openConnection();
                BufferedReader brIn = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;

                while ((line = brIn.readLine()) != null) {
                    xmlData += line;
                }

            } catch(MalformedURLException mURL) {
                System.out.println("EXCEPTION: Malformed URL!");
            } catch(IOException ioE) {
                System.out.println("EXCEPTION: I/O Exception! \"There was an error trying to open" +
                        " a connection!\"");
            }

            return xmlData;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println("************PRINTING XML DATA*****************");
            System.out.println(result);
            System.out.println("Finished Printing XML data");
            xmlResult = result;
        }

    }



}

