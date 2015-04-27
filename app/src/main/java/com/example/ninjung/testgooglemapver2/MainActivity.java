package com.example.ninjung.testgooglemapver2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
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
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends FragmentActivity implements GoogleMap.OnMapClickListener{

    final int RQS_GooglePlayServices = 1;
    private GoogleMap map;
    LatLng location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        LatLng sfsu = new LatLng(37.7223950, -122.4786140);
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sfsu, 13));
        Marker marker = map.addMarker(new MarkerOptions()
                .title("San Francisco State University")
                .position(sfsu));
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
                TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                TextView tvlng = (TextView) v.findViewById(R.id.tv_lng);
                tvLat.setText("Latitude: " + point.latitude);
                tvlng.setText("Longitude: " + point.longitude);
                ImageView image = (ImageView) v.findViewById(R.id.streetview);

                return  v;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
        markerOptions.title(point.latitude + " : " + point.longitude);


        // Animating to the touched position
        map.animateCamera(CameraUpdateFactory.newLatLng(point));

        // Placing a marker on the touched position
        map.addMarker(markerOptions);

        // Set marker to handle custom info window
        Marker marker = map.addMarker(markerOptions);
        marker.showInfoWindow();
    }
    public void getInfo(View view) {
        // Perform action on click
        MarkerOptions markerOptions = new MarkerOptions();
        if(location == null){
            location = new LatLng(37.7223950, -122.4786140); // Setting default at SFSU
        }
        // Setting the position for the marker
        markerOptions.position(location);

        // Connecting SF Park API
        new SFPark.FeedTask().execute(location.latitude, location.longitude);
    }

    public void setParking(View view) {
        // Perform action on click
        //MarkerOptions markerOptions = new MarkerOptions();
        if(location == null){
            location = new LatLng(37.7223950, -122.4786140); // Setting default at SFSU
        }
        new SFPark.FeedTask().execute(location.latitude, location.longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        // Setting the position for the marker
        markerOptions.position(location);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.darkgreen_parking));

        //NEED TO CHANGE THIS LINE
        markerOptions.title("Can park: "+SFPark.getXmlResult());
        map.clear();
        Marker marker1= map.addMarker(markerOptions);
        marker1.showInfoWindow();
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            //test comment
            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                TextView txtCanPark = (TextView) v.findViewById(R.id.parkingInfo);
                txtCanPark.setText(arg0.getTitle());


                final TextView snippetUi = ((TextView) v.findViewById(R.id.snippet));
                snippetUi.setText("snippet");
                // Returning the view containing InfoWindow contents
                return v;

            }
        });
    }
}
