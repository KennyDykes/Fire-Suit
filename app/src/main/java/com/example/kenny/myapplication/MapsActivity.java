package com.example.kenny.myapplication;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Uses Google Maps to display a firefighter's movements
 * Uses a Cloud Database called Parse.com
 */
public class MapsActivity extends FragmentActivity implements
      ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    /**
     * Marker will represent a person
     */
    Marker m;
    private GoogleApiClient mGoogleApiClient;
    /**
     * Holds the user's name
     */
    String username;
    /**
     * Boolean values that help signal if a temp is too high
     */
    boolean danger,status;
    /**
     * Used so that we can store longitude and latitude values to database
     */
    ParseGeoPoint point = new ParseGeoPoint(0,0);
    /**
     * Used for Requesting location updates
     */
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates;
    /**
     * Field stores location from which we will get longitude and latitude values
     */
    private Location mCurrentLocation;

    /**
     * Sets up time intervals of location requests and priority
     */

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Sets up Google Clients API
     */
    protected synchronized void setUpGoogleApiClientIfNeeded() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        createLocationRequest();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Parse.initialize(this, "Gd6M5GH29gbN0kwMGxjjBTciCYqJ9e2SMnxbgULl", "GY2ILsjo16tB71GUnqfSTte0u54LMbNIlliH8fL3"); //Intializses Parse using App ID and Client Key
        ParseUser.enableAutomaticUser();     //Creates a Automatic user instead of having to login
        ParseUser.getCurrentUser().saveInBackground(); //Saves user in the database
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        setUpMapIfNeeded();
        setUpGoogleApiClientIfNeeded();
        mRequestingLocationUpdates = true;

    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

         }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
     //   Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();


        }
        findLocations();
     camera(21);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

   
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }

    /**
     * Clears and redraws the map as a person's location changes
     * @param location Gets the current location of the user
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d("location Changed","Change" );
        if(mMap!=null)
        {
            mMap.clear();                                  //Clears map every time a location changes
        }
        mCurrentLocation = location;

        findLocations();                       // Function Call gets values from database and redraws map

    }

    /**
     * Maintains location updates in the resume lifecycle stage
     */
    @Override
    protected void onResume() {
        super.onResume();
        findLocations();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();

        }
    }

    /**
     * Google Client APIs isn't needed in the paused state so, we disconnect
     */
    @Override
    protected void onPause(){
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    /**
     * Google Client APIs isn't needed in the stopped state so, we disconnect
     */
    @Override
    protected void onStop(){
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

   @Override
   protected void onStart()
{
    super.onStart();
    mGoogleApiClient.connect();
    setUpMapIfNeeded();

}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed)
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
               mMap.setMyLocationEnabled(true);

            }
        }

        camera(21);
    }

    /**
     * This is where we c add markers and the color of the markers depends on what conditional statement is proven true
     *
     */
    private void setUpMap() {
        double latitude=0;
        double longitude=0;
        latitude =point.getLatitude();
        longitude=point.getLongitude();
        if (latitude !=0 && longitude != 0) {

                if (danger == false && status==false) {
                    m = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                            // .title("Marker")
                            .title(username).visible(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    m.setPosition(new LatLng(latitude, longitude));
                    //  danger=true;
                } else if (danger == true && status ==false) {
                    m = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                            // .title("Marker")
                            .title(username).visible(true)//+ "\n"+"Status:"+status)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    m.setPosition(new LatLng(latitude, longitude));
            }
            else if(status==true && danger==false || status==true && danger==true)
           {
               m = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                       // .title("Marker")
                       .title(username).visible(true)//+ "\n"+"Status:"+status)
                       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
               m.setPosition(new LatLng(latitude, longitude));
           }
        }

    }



    public void camera (int zoom)
    {
        final CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))      // Sets the center of the map to Mountain View
                .zoom(zoom)                   // Sets the zoom
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //temp=cameraPosition;
    }

    /**
     * Gets information from database representing a specific firefighter and adds into a List,
     * where eventually it will be represented as a marker on Google Maps
     */

    public void findLocations()
    {
        ParseQuery<ParseObject> Location= null;
        Location=ParseQuery.getQuery("Firefighter");                 //Parse Query for Firefighter database table
        Location.findInBackground(new FindCallback<ParseObject>(){
            @Override
            public void done(List<ParseObject> locList, ParseException e) {

                if (e == null) {

                    Log.d("location", "Retrieved " + locList.size() + " loc");

                        int i=0;
                        while(i<locList.size())                                  //Loop fills list with data from different Parse database columns
                        {
                            point=locList.get(i).getParseGeoPoint("Location");
                            username=locList.get(i).getString("Name");
                            status=locList.get(i).getBoolean("Status");
                            danger=locList.get(i).getBoolean("Pressure");
                            setUpMap();
                            i++;
                        }



                } else {
                    Log.d("location", "Error: " + e.getMessage());

                }

            }

        });


        setUpMap();                                                   // Draws Map

    }

}
