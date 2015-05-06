package com.example.ninjung.testgooglemapver2;


import android.location.Address;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Geocoder;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements GoogleMap.OnMapClickListener
        , OnStreetViewPanoramaReadyCallback {

    final int RQS_GooglePlayServices = 1;
    private GoogleMap map;
    LatLng location;
    int buttonCounter = 0;

    //ArrayList used to store SFPark information, this is set in processFinish().
    private ArrayList<AVL> sfpInfo = new ArrayList<AVL>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //used to clean database table if needed
        //DBHelper dbHandler = new DBHelper(this, null, null, 1);
        //dbHandler.cleanDB();

        // get a handle on GoogleMap Fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap(); //instantiate Google Map object
        location = new LatLng(37.7223950, -122.4786140);// set default location at SFSU

        // get a handle on streetViewPanorama fragment
        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this); // call back on the fragment to execute onStreetViewPanoramaReady()

        // enable current location
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13)); // set zoom on the map
        MarkerOptions markerOptions = new MarkerOptions() // add content on the marker
                .title(getAddress(location.latitude,location.longitude))
                .position(location);

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
        try
        {
            List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addressList.get(0);
            address = obj.getAddressLine(0) + ", " +obj.getAddressLine(1);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return address;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode == ConnectionResult.SUCCESS){
            Toast.makeText(getApplicationContext(),
                    "isGooglePlayServicesAvailable SUCCESS",
                    Toast.LENGTH_LONG).show();
        }else{
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, RQS_GooglePlayServices);
        }

    }
    /**
     * This method displays a marker on a location that users click on the map
     * @param point - contains Latitude and Longitude on the location that users click on the map
     */
    @Override
    public void onMapClick(LatLng point) {
        // Clears the previously touched position
        map.clear();

        //display street views on top of the map
        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        // add contents into infoWindow
        location= point;
        MarkerOptions markerOptions = new MarkerOptions()
                .title(getAddress(location.latitude,location.longitude))
                .position(location);
        Marker marker = map.addMarker(markerOptions);
        marker.showInfoWindow();

        // Animating to the touched position
        map.animateCamera(CameraUpdateFactory.newLatLng(point));

        //Handles custom info window on map click
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.windowlayout, null);
                LatLng point = marker.getPosition();

                //Text and image to be displayed in this custom window
                //Image will be changed to streetview picture later if possible
                TextView tvaddress = (TextView) v.findViewById(R.id.tv_address);
                tvaddress.setText(marker.getTitle());
                TextView tvrate = (TextView) v.findViewById(R.id.tv_rate);
                tvrate.setText(marker.getSnippet());
                //ImageView image = (ImageView) v.findViewById(R.id.streetview);
                return v;
            }
        });


    }

    /**
     * display parking information SFPark on an InfoWindow when user click the info button
     * @param view - connect to custom_info_window.xml to present data from SFPark on InfoWindow
     */
    public void getInfo(View view) {
            getSFParkInfo(location.latitude,location.longitude);// call the method to get SFPark information
            // Perform action on click
            MarkerOptions markerOptions = new MarkerOptions();
            // Setting the position for the marker
            markerOptions.position(location);
            markerOptions.title(getAddress(location.latitude, location.longitude));


            //NEED TO CHANGE THIS LINE
            if (sfpInfo.size() > 0) {
                System.out.println("size>1");
                markerOptions.snippet("Rate\n" + sfpInfo.get(0).getRATES().toString());
            } else {
                System.out.println("size=0");
                markerOptions.snippet("Rate: Undetected");
            }

            map.clear();//clear marker on the map
            Marker marker = map.addMarker(markerOptions);
            marker.showInfoWindow();
            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.windowlayout, null);
                LatLng point = marker.getPosition();

                //Text and image to be displayed in this custom window
                //Image will be changed to streetview picture later if possible
                TextView tvaddress = (TextView) v.findViewById(R.id.tv_address);
                tvaddress.setText(marker.getTitle());
                TextView tvrate = (TextView) v.findViewById(R.id.tv_rate);
                tvrate.setText(marker.getSnippet());
                //ImageView image = (ImageView) v.findViewById(R.id.streetview);
                return v;
            }
        });
    }

    /**
     * perform when a user click on the parking button
     * the parking marker will replace the current marker and current marker location will be stored in the Database
     * @param view
     */
    public void setParking(View view) {

        //storing location to DB
        buttonCounter++;
        if((buttonCounter%2) == 1) {
            DBHelper dbHandler = new DBHelper(this, null, null, 1);
            dbHandler.addLocation(location);
        }
        else {
            DBHelper dbHandler = new DBHelper(this, null, null, 1);

            LatLng parked = dbHandler.findLocation(dbHandler.getNaxID());

            if (parked != null) {
                Toast.makeText(getApplicationContext(),
                        "Lat: " + parked.latitude + "\nLong: " + parked.longitude,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "No records found.",
                        Toast.LENGTH_LONG).show();
            }
        }

        // Replace a previous marker to a parking marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.darkgreen_parking));

        // call the method to get SFPark information
        getSFParkInfo(location.latitude,location.longitude);

        // add information from SFParking into InfoWindow
        if (sfpInfo.size() > 0) {
            markerOptions.title(getAddress(location.latitude, location.longitude));
            markerOptions.snippet("Rate\n"+sfpInfo.get(0).getRATES().toString());
        } else {
            markerOptions.title(getAddress(location.latitude, location.longitude));
            markerOptions.snippet("Rate: Undetected");
        }
        // delete previous markers on the map
        map.clear();

        //present information on Info Window
        Marker marker = map.addMarker(markerOptions);
        marker.showInfoWindow();
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.windowlayout, null);
                LatLng point = marker.getPosition();

                //Text and image to be displayed in this custom window
                //Image will be changed to streetview picture later if possible
                TextView tvaddress = (TextView) v.findViewById(R.id.tv_address);
                tvaddress.setText(getAddress(point.latitude, point.longitude));
                TextView tvrate = (TextView) v.findViewById(R.id.tv_rate);
                tvrate.setText(marker.getSnippet());
                //ImageView image = (ImageView) v.findViewById(R.id.streetview);
                return v;
            }
        });
    }

    /**
     * display last 5 previous parking on the map when user click showRecentParking button
     * @param view - connect to custom_info_window.xml to present data from SFPark on InfoWindow
     */
    public void showRecentParking(View view) {
        //retrieve last 5 previous parking information from the DataBase
        DBHelper dbHandler = new DBHelper(this, null, null, 1);
        ArrayList<LatLng> locations = dbHandler.getRecentParking();

        // zoom out the map to present the previous parking appropriately
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));

        // display previous parking in different color
        for (int i=0;i<locations.size();i++) {
            LatLng loc = locations.get(i);
            System.out.println("latitude: "+loc.latitude+", longitude: "+loc.longitude);
            BitmapDescriptor bitmapMarker;
            if(i+1==1){
                bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
            }else if(i+1==2){
                bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
            }else if(i+1==3){
                bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            }else if(i+1==4){
                bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            }else {
                bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
            }

            // get information from SFPark
            getSFParkInfo(loc.latitude,loc.longitude);
            String sfInfo;
            if(sfpInfo.size()>0){
                sfInfo = "Rate\n"+sfpInfo.get(0).getRATES().toString();
            }else{
                sfInfo = "Rate : Undetected";
            }
            // add information into InfoWindow
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(loc.latitude, loc.longitude))
                    .title(getAddress(loc.latitude,loc.longitude))
                    .snippet(sfInfo)
                    .icon(bitmapMarker));
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
}
