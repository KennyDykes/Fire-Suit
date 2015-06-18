package com.example.kenny.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * The Tracker Activity is where the firefighter's longitude and latitude values are actually being tracked
 * and sent to our Parse database
 */

public class TrackerActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    /**
     * The two strings will hold the string values that was passed through intents
     * grab will hold the user's name
     * temp will hold the MAC Address
     */
    String grab,temp;
    /**
     * Declaration of Image Buttons
     * help is a button a firefighter can press to signal for assistance
     * connection is to reconnect to an android device
     */
    ImageButton help,connection;
    /**
     * Gets the current location of firefighter
     */
    Location mCurrentLocation;

    /**
     * Used for Location updates
     */
    private LocationRequest mLocationRequest;
    /**
     * Boolean used to determine if we should request location updates
     */
    boolean mRequestingLocationUpdates;

    private GoogleApiClient mGoogleApiClient;
    /**
     * Delcared to handle bytes we attempt to send the out
     */
    private OutputStream mmOutStream;
    BluetoothDevice device;
    /**
     * Different Textviews that will hold and display longitude values,latitude values , and user's name
     */
    TextView mLatitudeTextView, mLongitudeTextView,mStatus, name;
    /**
     * Parse Object used to store to database
     */
    ParseObject glo;
    /**
     * Gives us the ability to send a firefighter's current location to the database
     */
    ParseGeoPoint loc;
    protected static final String TAG = "location-updates";

    InputStream tmpIn = null;
    OutputStream tmpOut = null;


    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;


    private BluetoothSocket btSocket = null;
    private BluetoothAdapter mBluetoothAdapter = null;




    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            startLocationUpdates();

        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        }
        updateUI();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in         // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        mGoogleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
            Parse.initialize(this, "Gd6M5GH29gbN0kwMGxjjBTciCYqJ9e2SMnxbgULl", "GY2ILsjo16tB71GUnqfSTte0u54LMbNIlliH8fL3");
            ParseUser.enableAutomaticUser();
            ParseUser.getCurrentUser();
            ParseUser.getCurrentUser().saveInBackground();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent get = getIntent();
        grab = get.getStringExtra("username");  //Gets Firefighter's name
        temp= get.getStringExtra("address");    //Gets Bluetooth MAC Address
        help = (ImageButton) findViewById(R.id.helpButton); //Button for sending help signal
        connection= (ImageButton)findViewById(R.id.connectButton); //Button for reconnecting Bluetooth device

        glo = new ParseObject("Firefighter"); //Creates an instance of the Firefighter table in the database
        glo.put("Pressure", false);   // Places false in Pressure column of the FireFighter table
        glo.put("Name", grab);       // Places the firefighter's name in Name column of the FireFighter table
        glo.saveInBackground();

        name = (TextView) findViewById(R.id.nameText);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);

        mStatus=(TextView)findViewById(R.id.status_text);
        name.setText(grab);

        setUpGoogleApiClientIfNeeded();

        mRequestingLocationUpdates = true;
        //create device and set the MAC address
        address = temp;                                                   //MAC Address
        device = mBluetoothAdapter.getRemoteDevice(address);             //Creates Bluetooth socket

        /**
         * Reconnects the Bluetooth device when connection is clicked
         */

      connection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Attempting Connection", Toast.LENGTH_LONG).show();
                try {
                    btSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    btSocket.connect();
                    // btSocket.connect();
                } catch (IOException e) {
                    try {
                        btSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }


            }
        });


/**
 * Creates initial Bluetooth connection
 */

       try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            btSocket.connect();
           // btSocket.connect();
        } catch (IOException e) {
          try {
             btSocket.close();
          } catch (IOException e1) {
              e1.printStackTrace();
           }
        }

/**
 * Sends a signal to the database when help is clicked
 */
        help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Sending Signal", Toast.LENGTH_LONG).show();
                glo.put("Pressure", true);                          // Sends a true value to the Pressure column of Firefighter's table of Parse database
                glo.saveInBackground();
            }
        });

    }

    /**
     * Creates a Bluetooth socket
     * @param device represents Bluetooth device
     * @return
     * @throws IOException
     */
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }


    protected synchronized void setUpGoogleApiClientIfNeeded() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                 .build();
                 createLocationRequest();


    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Every time a location is changed the database is updated
     * and values are checked
     * @param location current location of the firefighter
     */
    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        loc = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        glo.put("Location", loc);            //Updates longitude and latitude values in Location column of Parse database
        glo.saveInBackground();             //saves info into database
        updateUI();                        //Function Call displays longitude and latitude changes
        InputStream mmInStream;
        try {
            //Create I/O streams for connection
            tmpIn = btSocket.getInputStream();

        } catch (IOException e) {
        }

        mmInStream = tmpIn;        //Gets the connection stream
        byte[] buffer = new byte[256];
        int bytes;
        int i=0;
        // Keep looping to listen for received messages
           try {
                bytes = mmInStream.read(buffer);            //read bytes from input buffer
                String readMessage = new String(buffer, 0, bytes);
                String []temp=  readMessage.split("/");  // the backslash indicates the end of a message

               double temperature=  Double.valueOf(temp[0]);          //Reads Temperature values
               {
                   if (readMessage != null)

                   {
                       glo.put("present", true);           //Check if Bluetooth module maintains connection
                       glo.saveInBackground();

                     if (temperature >= 100) {
                            glo.put("Status", true);      //If the temperature value is over 100 degrees we send a true to the Status column in the database
                            glo.saveInBackground();
                        }
                     else if (temperature < 98) {
                            glo.put("Status", false);   //If the temperature is under 98 degrees we send a false
                            glo.saveInBackground();
                        }

                   }
                   else
                   {
                       glo.put("present",null);   // We lost Bluetooth connection
                       glo.saveInBackground();
                   }
               }

            } catch (IOException e) {

            }



    }

    /**
     * Updates values that are shown on the UI
     */

    private void updateUI() {
        mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
        if(glo.get("present")==null )
        {
           mStatus.setText("Disconnected");
        }
        else
         {
            mStatus.setText("Connected");
         }
    }

    /**
     * Creates Google API connection
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();


    }

    /**
     * Disconnects Bluetooth and Google APIs connections
     */
    @Override
public void onDestroy() {
        super.onDestroy();
        try {
            btSocket.close();                   //Closes Bluetooth connection
        } catch (IOException e) {
            e.printStackTrace();
        }

       if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }


    }
    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();

        }

    }

    /**
     * Maintains connection so firefighter may put device in pocket and still be tracked
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
             startLocationUpdates();
             }

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tracker, menu);
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


}

