package com.example.ninjung.testgooglemapver2;

import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.location.Geocoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.GoogleMap.*;

public class MainActivity extends FragmentActivity implements OnMapClickListener
        , OnStreetViewPanoramaReadyCallback {

    final int RQS_GooglePlayServices = 1;
    final int DATABASE_VERSION = 2;
    private GoogleMap map;
    LatLng location;
    int buttonCounter = 0;
    private String action_park = "Park";
    //ArrayList used to store SFPark information, this is set in processFinish().
    private ArrayList<AVL> sfpInfo = new ArrayList<AVL>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //used to clean database table if needed
        DBHelper dbHandler = new DBHelper(this, null, null, DATABASE_VERSION);
        //dbHandler.cleanDB();

        // get a handle on GoogleMap Fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap(); //instantiate Google Map object



        //check to see if user has unparked car, if so use that location when app opens
        if((dbHandler.getRowCountParked()) > 0){
            buttonCounter++;
            location = dbHandler.findLocationParked();
            Toast.makeText(getApplicationContext(),
                    "Saved car location detected.\n" +
                    "Retrieving parked coordinates...",
                    Toast.LENGTH_LONG).show();
            Button p1_button = (Button)findViewById(R.id.setparking);
            p1_button.setText("Unpark");
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));// set zoom on the map
        }
        else {
            location = new LatLng(37.7223950, -122.4786140);// set default location at SFSU
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13)); // set zoom on the map
        }

        // get a handle on streetViewPanorama fragment
        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this); // call back on the fragment to execute onStreetViewPanoramaReady()


        map.setMyLocationEnabled(true); // enable current location
        MarkerOptions markerOptions = new MarkerOptions() // add content on the marker
                .title(getAddress(location.latitude,location.longitude))
                .position(location);

        //if user did not unpark car, change icon to parked icon from default
        if((dbHandler.getRowCountParked()) > 0)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.darkgreen_parking));

        // add the marker on the map
        Marker marker = map.addMarker(markerOptions);
        marker.showInfoWindow(); // display infoWindow
        map.setOnMapClickListener(this); // call onMapClick() when users click on the map
    }

    /**
     * This method converts latitude and longitude to street address
     * and return it as String
     *
     * @param lat - latitude
     * @param lng - longitude
     * @return String: Street address based on the given latitude and longitude
     */
    public String getAddress(double lat, double lng) {
        String address = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
             if( (addressList.size() > 0) && (addressList != null) ) {
                Address obj = addressList.get(0);
                address = obj.getAddressLine(0) + ", " + obj.getAddressLine(1);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return address;
    }


    //OnCreateOptionMenu and OnOptionsItemSelected handles action bar
    /**
     * OnCreateOptionMenu: Initialize action bar
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.ac_setparking);
        item.setTitle(action_park);
        return true;
    }

    /**
     * OnOptionsItemSelected: Responds to click on action bar buttons
     * @param menuItem
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        View v = new View(this);
        switch(menuItem.getItemId()) {
            case R.id.ac_getinfo:
                getInfo(v);
                return true;
            case R.id.ac_setparking:
                setParking(v);

                if ((buttonCounter % 2) == 1) {
                    action_park = "Unpark";
                } else {
                    action_park = "Park";
                }
                supportInvalidateOptionsMenu();
                return true;
            case R.id.ac_showRecentParking:
                showRecentParking(v);
                return true;
            case R.id.ac_getDirection:
                getRoute(v);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode == ConnectionResult.SUCCESS) {
            Toast.makeText(getApplicationContext(),
                    "isGooglePlayServicesAvailable SUCCESS",
                    Toast.LENGTH_LONG).show();
        } else {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
        }

    }
    /**
     * This method displays a marker on a location that users click on the map
     * @param point - contains Latitude and Longitude on the location that users click on the map
     */
    @Override
    public void onMapClick(LatLng point) {
        // Clear the previously touched position
        map.clear();

        // Clear previous SFPark information
        sfpInfo.clear();

        location= point;
        // call the method to get SFPark information
        getSFParkInfo(location.latitude, location.longitude);

        //display street views on top of the map
        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        // Animating to the touched position
        map.animateCamera(CameraUpdateFactory.newLatLng(point));

        // add contents into infoWindow
        MarkerOptions markerOptions = new MarkerOptions()
                .title(getAddress(location.latitude,location.longitude))
                .position(location);

        displayInfoWindow(markerOptions);
    }

    /**
     * display parking information SFPark on an InfoWindow when user click the info button
     * @param view - connect to custom_info_window.xml to present data from SFPark on InfoWindow
     */
    public void getInfo(View view) {
            MarkerOptions markerOptions = new MarkerOptions();
            // Setting the position for the marker
            markerOptions.position(location);
            markerOptions.title(getAddress(location.latitude, location.longitude));

            if (sfpInfo.size() > 0) {
                markerOptions.snippet("RATE\n" + sfpInfo.get(0).getRATES().toString());
            } else {
                markerOptions.snippet("RATE: Undetected");
            }

            map.clear();//clear marker on the map
            displayInfoWindow(markerOptions);
    }

    /**
     * perform when a user click on the parking button
     * the parking marker will replace the current marker and current marker location will be stored in the Database
     * @param view
     */
    public void setParking(View view) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.darkgreen_parking));
        buttonCounter++;

        //if button is pushed for the first time in a cycle
        if ((buttonCounter % 2) == 1) {

            // Setting the position for the marker
            markerOptions.position(location);

            //storing location to DB
            DBHelper dbHandler = new DBHelper(this, null, null, DATABASE_VERSION);
            dbHandler.addLocation(location);

            //clear the saved state table, then add to it
            dbHandler.deleteLocationParked();
            dbHandler.addLocationParked(location);

            //maintenance. if the size of the table is above 5 clean the least recently used row
            if ((dbHandler.getRowCount()) > 5)
                dbHandler.deleteLocation();

            Toast.makeText(getApplicationContext(),
                    "Parking location saved.",
                    Toast.LENGTH_LONG).show();

            getSFParkInfo(location.latitude, location.longitude);// call the method to get SFPark information

            if (sfpInfo.size() > 0) {
                markerOptions.title(getAddress(location.latitude, location.longitude));
                markerOptions.snippet("Rate\n" + sfpInfo.get(0).getRATES().toString());
            } else {
                markerOptions.title(getAddress(location.latitude, location.longitude));
                markerOptions.snippet("Rate: Undetected");
            }

            map.clear();
            displayInfoWindow(markerOptions);

            Button p1_button = (Button)findViewById(R.id.setparking);
            p1_button.setText("Unpark");
        } else {

            //if parked button hit a second time in cycle
            DBHelper dbHandler = new DBHelper(this, null, null, DATABASE_VERSION);

            //unpark location from saved state
            dbHandler.deleteLocationParked();

            //retrieve location from history table
            LatLng parked = dbHandler.findLocation(dbHandler.getNaxID());
            location = parked;

            if (parked != null) {
                Toast.makeText(getApplicationContext(),
                        "Retrieving... unparked.",
                        Toast.LENGTH_LONG).show();

                //move camera to retrieved location
                onMapClick(location);
                markerOptions.position(location);
                map.clear();
                displayInfoWindow(markerOptions);
            } else {
                Toast.makeText(getApplicationContext(),
                        "No records found.",
                        Toast.LENGTH_LONG).show();
            }
            Button p1_button = (Button)findViewById(R.id.setparking);
            p1_button.setText("Park");
        }
    }

    /**
     * display last 5 previous parking on the map when user click showRecentParking button
     * @param view - connect to custom_info_window.xml to present data from SFPark on InfoWindow
     */
    public void showRecentParking(View view) {
        //retrieve last 5 previous parking information from the DataBase
        DBHelper dbHandler = new DBHelper(this, null, null, DATABASE_VERSION);
        ArrayList<LatLng> locations = dbHandler.getRecentParking();

        if ((locations != null) && (locations.size() > 0)) {

            // zoom out the map to present the previous parking appropriately
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));

            // display last 5 parking in different color
            for (int i = 0; i < locations.size(); i++) {
                LatLng loc = locations.get(i);
                BitmapDescriptor bitmapMarker;
                if (i + 1 == 1) {
                    bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
                } else if (i + 1 == 2) {
                    bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                } else if (i + 1 == 3) {
                    bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                } else if (i + 1 == 4) {
                    bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                } else {
                    bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
                }

                // add information into InfoWindow
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(loc.latitude, loc.longitude))
                        .title(getAddress(loc.latitude, loc.longitude))
                        .icon(bitmapMarker));
            }
        }
        else {
            Toast.makeText(getApplicationContext(),
                    "No records found.",
                    Toast.LENGTH_LONG).show();
        }

    }


    private void getSFParkInfo(double latitude, double longtitude){

        SFPark.FeedTask feedTask = new SFPark.FeedTask(new AsyncResponse() {

            /**
             * Used to retrieve information from onPostExecute() in SFPark.java.
             * @param avl Output from onPostExecute() is passed through here.
             */

            @Override
            public void processFinish(ArrayList<AVL> avl) {

                //Assign ArrayList avl to sfpInfo so it can be used in this file.
                sfpInfo = avl;

                //Printing for debugging purposes, delete this later...
                if (avl.size() > 0) {
                    System.out.println("The rate of avl.get(0) in MainActivity is " + avl.get(0).getRATES());
                } else {
                    System.out.println("The size of AVL is 0 in MainActivity.java!");
                }
            }
        });
        //Request information from the SFPark API.
        feedTask.execute(latitude, longtitude);
    }

    /**
     * a callback method to retrieve a non-null instance of StreetViewPanorama, ready to be used.
     * @param streetViewPanorama
     */
    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        streetViewPanorama.setPosition(location);
    }

    /**
     * add information into infoWindow
     * @param markerOptions - contains marker character and information
     */
    private void displayInfoWindow(MarkerOptions markerOptions){
        Marker marker = map.addMarker(markerOptions);
        marker.showInfoWindow();
        map.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.windowlayout, null);
                LatLng point = marker.getPosition();

                //Text and image to be displayed in this custom window
                TextView tvaddress = (TextView) v.findViewById(R.id.tv_address);
                tvaddress.setText(getAddress(point.latitude, point.longitude));
                TextView tvrate = (TextView) v.findViewById(R.id.tv_rate);
                tvrate.setText(marker.getSnippet());
                return v;
            }
        });
    }
    public void getRoute(View view){
        DBHelper dbHandler = new DBHelper(this, null, null, DATABASE_VERSION);
        LatLng location = dbHandler.getLastestParking();
        //String uri = "http://maps.google.com/maps?saddr="+"37.757246, -122.492774"+"&daddr="+location.latitude+","+location.longitude+"&dirflg=w";
        //Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+location.latitude+","+ location.longitude+"&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

}
