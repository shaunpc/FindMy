package com.spc.findmy;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Location sample.
 * <p/>
 * Demonstrates use of the Location API to retrieve the last known location for a device.
 * This sample uses Google Play services (GoogleApiClient) but does not need to authenticate a user.
 * See https://github.com/googlesamples/android-google-accounts/tree/master/QuickStart if you are
 * also using APIs that need authentication.
 */
public class MainActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, OnMapReadyCallback {
    protected static final String TAG = "FindMy";
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;

    Button unicornButton;
    Button specialButton;

    Marker melbourne;
    LatLng MELBOURNE;

    SupportMapFragment mapFragment;
    GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));
        buildGoogleApiClient();

        // find the Map fragment so we can work with it...
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            Log.i(TAG, "Can't call getMapAsync as mapFragment is null - not sure what to do!");
        } else {
            mapFragment.getMapAsync(this);
        }

        unicornButton = (Button) findViewById(R.id.button1);
        unicornButton.setText(Html.fromHtml(getString(R.string.unicorn_button_text)));

        specialButton = (Button) findViewById(R.id.button2);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


//        public final double latitude
//        Latitude, in degrees. This value is in the range [-90, 90].
//        public final double longitude
//        Longitude, in degrees. This value is in the range [-180, 180).


        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));

        MELBOURNE = new LatLng(-37.813, 144.962);
        melbourne = googleMap.addMarker(new MarkerOptions()
                .position(MELBOURNE)
                .title("Melbourne")
                .snippet("Population: 4,137,400")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));


        // Sets the map type to be "hybrid"
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Sets my-location button
        googleMap.setMyLocationEnabled(true);
        // Set the map toolbar to be enabled

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Ensure other functions can access this map when ready
        map = googleMap;
    }

    public void onUnicornButton(View v) {
        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(melbourne.getPosition().latitude, melbourne.getPosition().longitude)) // Sets the center of the map to Melbourne
                .zoom(10)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mLatitudeText.setText(String.valueOf(melbourne.getPosition().latitude));
        mLongitudeText.setText(String.valueOf(melbourne.getPosition().longitude));
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
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
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
}
