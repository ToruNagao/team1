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
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

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

public class MainActivity extends FragmentActivity implements GoogleMap.OnMapClickListener {

    final int RQS_GooglePlayServices = 1;
    private static final String RECENT_PARKING_FIRST = "R.drawable.pink_marker_a";
    private static final String RECENT_PARKING_SECOND = "R.drawable.purple_marker_b";
    private static final String RECENT_PARKING_THIRD = "R.drawable.blue_marker_c";
    private static final String RECENT_PARKING_FOURTH = "R.drawable.paleblue_marker_d";
    private static final String RECENT_PARKING_FIFTH = "R.drawable.brown_marker_e";
    private GoogleMap map;
    LatLng location;
    int buttonCounter = 0;

    //ArrayList used to store SFPark information, this is set in processFinish().
    private ArrayList<AVL> sfpInfo = new ArrayList<AVL>();

    //used to clean database table if needed
    //DBHelper dbHandler = new DBHelper(this, null, null, 1);
    //dbHandler.cleanDB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        location = new LatLng(37.7223950, -122.4786140);//default location at SFSU
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
        Marker marker = map.addMarker(new MarkerOptions()
                .title("San Francisco State University")
                .position(location));
        marker.showInfoWindow();
        map.setOnMapClickListener(this);

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
                //TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                //TextView tvlng = (TextView) v.findViewById(R.id.tv_lng);
                TextView tvaddress = (TextView) v.findViewById(R.id.tv_address);
                tvaddress.setText(getAddress(point.latitude, point.longitude));
                //tvlng.setText("Longitude: " + point.longitude);
                ImageView image = (ImageView) v.findViewById(R.id.streetview);
                return  v;
            }
        });
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
     * This method displays a marker when users click on the map
     * and return it as String
     */
    @Override
    public void onMapClick(LatLng point) {
        // Clears the previously touched position
        map.clear();

        location= point;
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        markerOptions.position(point);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(getAddress(point.latitude, point.longitude));

        // Animating to the touched position
        map.animateCamera(CameraUpdateFactory.newLatLng(point));

        // Placing a marker on the touched position
        map.addMarker(markerOptions);

        // Set marker to handle custom info window
        Marker marker = map.addMarker(markerOptions);
        marker.showInfoWindow();
    }

    /*display parking information when user click info button*/
    public void getInfo(View view) {
            // Perform action on click
            MarkerOptions markerOptions = new MarkerOptions();
            // Setting the position for the marker
            markerOptions.position(location);

            getSFParkInfo(location.latitude,location.longitude);// call the method to get SFPark information

            //NEED TO CHANGE THIS LINE
            if (sfpInfo.size() > 0) {
                markerOptions.title(getAddress(location.latitude, location.longitude));
                markerOptions.snippet("Rate\n" + sfpInfo.get(0).getRATES().toString());
            } else {
                markerOptions.title(getAddress(location.latitude, location.longitude));
                markerOptions.snippet("Rate: Undetected");
            }

            map.clear();//clear marker on the map
            displayParkingInfo(markerOptions, "getInfo");


    }

    public void setParking(View view) {
        // Perform action on click
        //MarkerOptions markerOptions = new MarkerOptions();

        MarkerOptions markerOptions = new MarkerOptions();
        // Setting the position for the marker
        markerOptions.position(location);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.darkgreen_parking));

        //storing location to DB
        buttonCounter++;
        if((buttonCounter%2) == 1) {
            DBHelper dbHandler = new DBHelper(this, null, null, 1);

            Location parked =
                    new Location(location.latitude, location.longitude);

            dbHandler.addLocation(parked);
        }
        else {
            DBHelper dbHandler = new DBHelper(this, null, null, 1);

            Location parked = dbHandler.findLocation(dbHandler.getRowCount());

            if (parked != null) {
                Toast.makeText(getApplicationContext(),
                        "Lat: " + parked.getLatitude() + "\nLong: " + parked.getLongitude(),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "No records found.",
                        Toast.LENGTH_LONG).show();
            }
        }

        getSFParkInfo(location.latitude,location.longitude);// call the method to get SFPark information

        if (sfpInfo.size() > 0) {
            markerOptions.title(getAddress(location.latitude, location.longitude));
            markerOptions.snippet("Rate\n"+sfpInfo.get(0).getRATES().toString());
        } else {
            markerOptions.title(getAddress(location.latitude, location.longitude));
            markerOptions.snippet("Rate: Undetected");
        }

        map.clear();
        displayParkingInfo(markerOptions,"setParking");
    }

    /**
     *
     * @param view connect to custom_info_window.ext to present data from SFPark on InfoWindow
     */
    public void showRecentParking(View view) {
        //retrieve data from DB
        System.out.println("Show recent parking");
        DBHelper dbHandler = new DBHelper(this, null, null, 1);

        ArrayList<Location> locations = dbHandler.getRecentParking();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        for (int i=0;i<locations.size();i++) {
            Location loc = locations.get(i);
            System.out.println("latitude: "+loc.getLatitude()+", longitude: "+loc.getLongitude());
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
            getSFParkInfo(loc.getLatitude(),loc.getLongitude());
            String sfInfo;
            if(sfpInfo.size()>0){
                sfInfo = "Rate\n"+sfpInfo.get(0).getRATES().toString();
            }else{
                sfInfo = "Rate : Undetected";
            }
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title(getAddress(loc.getLatitude(),loc.getLongitude()))
                    .snippet(sfInfo)
                    .icon(bitmapMarker));
        }
    }
    /*display information from SFPark into info window and change style of the marker*/
    public void displayParkingInfo(MarkerOptions markerOptions, final String type){
        Marker marker1= map.addMarker(markerOptions);// add a new marker
        marker1.showInfoWindow();

        //Display Rate from SFPark on infoWindow
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                TextView txtCanPark = (TextView) v.findViewById(R.id.parkingInfo);
                txtCanPark.setText(marker.getTitle());


                TextView snippetUi = ((TextView) v.findViewById(R.id.snippet));
                snippetUi.setText(marker.getSnippet());

                ImageView icon = ((ImageView) v.findViewById(R.id.badge));
                if(type.equalsIgnoreCase("setParking")){
                    icon.setImageResource(R.drawable.darkgreen_parking);
                }else{
                    icon.setImageResource(R.drawable.red_marker_info);
                }
                return  v;
            }
        });
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
}
