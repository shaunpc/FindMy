package com.spc.findmy;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.TextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Location sample.
 * Demonstrates use of the Location API to retrieve the last known location for a device.
 * This sample uses Google Play services (GoogleApiClient) but does not need to authenticate a user.
 * See https://github.com/googlesamples/android-google-accounts/tree/master/QuickStart if you are
 * also using APIs that need authentication.
 */
public class MainActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, OnMapReadyCallback {
    protected static final String TAG = "FindMy";

    // Store key variables in the onSaveInstanceState call (cleaner than PrefsFile)
    static final String UNICORN_MODE = "UnicornMode";


    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
//    protected TextView mLatitudeText;
//    protected TextView mLongitudeText;

    View mainButtons;          // initial buttons
    View unicornButtons;       // detailed button view when main UNICORN button pressed

    Button unicornButton;
    Boolean unicorn_mode;

    SupportMapFragment mapFragment;
    GoogleMap map;

    // create an array of Unicorns
    Unicorn unicorns[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the main view
        setContentView(R.layout.activity_main);

        // find the Map fragment so we can work with it...
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            Log.i(TAG, "Can't call getMapAsync as mapFragment is null - not sure what to do!");
        } else {
            mapFragment.getMapAsync(this);
        }

        // Get the connection to the Google API sorted
        Log.i(TAG, "...buildGoogleApiClient");
        buildGoogleApiClient();

        // Get the ID of the main button view, and the unicorn scrollable button view
        Log.i(TAG, "...mainButtons");
        mainButtons = findViewById(R.id.main_buttons);
        Log.i(TAG, "...unicornButtons");
        unicornButtons = findViewById(R.id.unicorn_buttons);

        // Put some funky coloured text in the UNICORN button
        unicornButton = (Button) findViewById(R.id.button_unicorn);
        unicornButton.setText(Html.fromHtml(getString(R.string.unicorn_button_text)));

        // setting start-mode, may get overwritten onRestoreInstanceState
        unicorn_mode = false;

        // Set up all the unicorn names, etc.
        createUnicornArray();

        Log.i(TAG, "...done onCreate");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Center of the World : Latitude 0 / Longitude 0"));

        // Sets the map type to be "hybrid"
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Sets my-location button
        googleMap.setMyLocationEnabled(true);
        // Set the map toolbar to be enabled

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);


        // Setting a custom info window adapter for the google map
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.map_marker, null);

                // get and set the TextViews to set Unicorn details
                TextView tvName = (TextView) v.findViewById(R.id.markerName);
                TextView tvSnippet = (TextView) v.findViewById(R.id.markerSnippet);
                tvName.setText(arg0.getTitle());
                tvSnippet.setText(arg0.getSnippet());

                // Returning the view containing InfoWindow contents
                return v;

            }
        });
        // Ensure other functions can access this map when ready
        map = googleMap;
    }


    public void onStartUnicorns(View v) {
        Log.i(TAG, "...onStartUnicorns");
        // remove the default buttons from view, and bring up the special UNICORN ones
        mainButtons.setVisibility(View.GONE);
        unicornButtons.setVisibility(View.VISIBLE);
        unicorn_mode = true;
    }

    @Override
    public void onBackPressed() {

        if (unicorn_mode) {
            // bring the default buttons back, remove the special UNICORN ones
            mainButtons.setVisibility(View.VISIBLE);
            unicornButtons.setVisibility(View.GONE);
            unicorn_mode = false;
        } else {
            super.onBackPressed();
        }
    }

    public void onStartSpecial(View v) {

        //TODO
        Toast.makeText(getApplicationContext(), "Can't do anything special yet", Toast.LENGTH_SHORT).show();

    }


    public void onUnicornButton(View v) {

        int i;

        switch (v.getId()) {
            case R.id.button1:
                i = 1;
                break;
            case R.id.button2:
                i = 2;
                break;
            case R.id.button3:
                i = 3;
                break;
            case R.id.button4:
                i = 4;
                break;
            case R.id.button5:
                i = 5;
                break;
            case R.id.button6:
                i = 6;
                break;
            case R.id.button7:
                i = 7;
                break;
            case R.id.button8:
                i = 8;
                break;
            default:
                i = 0;
                break;
        }

        Toast.makeText(getApplicationContext(), "Unicorn " + unicorns[i].getName() + " is off.... but where?", Toast.LENGTH_SHORT).show();

        // relocate the appropriate Unicorn
        unicorns[i].relocate(map);
        Log.i(TAG, "Unicorn " + i + " (" + unicorns[i].getName() + ") moved to LatLng " + unicorns[i].getLocation());

        // Construct a CameraPosition and move there...
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(unicorns[i].getLocation())
                .zoom(10)   // 0 is the whole world, the higher the number, the more detailed/closer view
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    public void onStartMyself(View v) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Toast.makeText(getApplicationContext(),
                    "You are at Latitude:" + mLastLocation.getLatitude() +
                            " / Longitude:" + mLastLocation.getLongitude()
                    , Toast.LENGTH_SHORT).show();
            // Construct a CameraPosition focusing on where we are and animate the camera to that position.
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .zoom(15)                    // Sets the zoom to be quite detailed
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(5)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            Toast.makeText(getApplicationContext(), "Can't find myself for some reason!", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
// Provides a simple way of getting a device's location and is well suited for
// applications that do not require a fine-grained location and that do not need location
// updates. Gets the best and most recent location currently available, which may be null
// in rare cases when a location is not available.
        Log.i(TAG, "API: onConnected...");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (mLastLocation != null) {
//            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
//            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
//        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
// onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /*
    * Called by Google Play services if the connection to GoogleApiClient drops because of an
    * error.
    */
    public void onDisconnected() {
        Log.i(TAG, "Disconnected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
// The connection to Google Play services was lost for some reason. We call connect() to
// attempt to re-establish the connection.
        Log.i(TAG, "API: Connection suspended");
        mGoogleApiClient.connect();
    }

    // Rely on system calling onSaveInstanceState when stopping to save the key variables
    // alongside the other 'bundle' instance state.
    // Then as system calls onRestoreInstanceState() only if there is a saved state to restore
    // (eg if changing screen orientation) then we pull the variables back from the bundle
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current converter state
        savedInstanceState.putBoolean(UNICORN_MODE, unicorn_mode);
        Log.i(TAG, "...saving unicorn_mode as " + unicorn_mode);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        unicorn_mode = savedInstanceState.getBoolean(UNICORN_MODE, false);

        // If already in UNICORN mode, then bring buttons to fore...
        Log.i(TAG, "...restored unicorn_mode is " + unicorn_mode);
        if (unicorn_mode) onStartUnicorns(unicornButtons);
    }

    public void createUnicornArray() {

        //Populate the Unicorn array
        unicorns[1] = new Unicorn(
                getResources().getString(R.string.nm_unicorn1),
                getResources().getString(R.string.ds_unicorn1),
                R.drawable.ic_unicorn1);
        unicorns[2] = new Unicorn(
                getResources().getString(R.string.nm_unicorn2),
                getResources().getString(R.string.ds_unicorn2),
                R.drawable.ic_unicorn2);
        unicorns[3] = new Unicorn(
                getResources().getString(R.string.nm_unicorn3),
                getResources().getString(R.string.ds_unicorn3),
                R.drawable.ic_unicorn3);
        unicorns[4] = new Unicorn(
                getResources().getString(R.string.nm_unicorn4),
                getResources().getString(R.string.ds_unicorn4),
                R.drawable.ic_unicorn4);
        unicorns[5] = new Unicorn(
                getResources().getString(R.string.nm_unicorn5),
                getResources().getString(R.string.ds_unicorn5),
                R.drawable.ic_unicorn5);
        unicorns[6] = new Unicorn(
                getResources().getString(R.string.nm_unicorn6),
                getResources().getString(R.string.ds_unicorn6),
                R.drawable.ic_unicorn6);
        unicorns[7] = new Unicorn(
                getResources().getString(R.string.nm_unicorn7),
                getResources().getString(R.string.ds_unicorn7),
                R.drawable.ic_unicorn7);
        unicorns[8] = new Unicorn(
                getResources().getString(R.string.nm_unicorn8),
                getResources().getString(R.string.ds_unicorn8),
                R.drawable.ic_unicorn8);

    }

}
