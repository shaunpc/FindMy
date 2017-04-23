package com.spc.findmy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.spc.findmy.Constants.MY_PERMISSIONS_REQUEST_LOCATION;

/**
 * Based on Google "Location" sample application.
 * Uses of the Location API from GooglePlayServices to retrieve the last known location for a
 * device, and to find address text of where Unicorns land!
  */
public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    protected static final String TAG = "FindMy";   // For debug logging purposes
    static final String UNICORN_MODE = "UnicornMode";   // Store key variables in the onSaveInstanceState call (cleaner than PrefsFile)
    protected GoogleApiClient mGoogleApiClient; // Provides the entry point to Google Play services.
    protected Location mLastLocation;   //R epresents a geographical location.
    View mainButtons;           // initial buttons
    View unicornButtons;        // detailed button view when main UNICORN button pressed
    Unicorn unicorns[];         // an array of Unicorns
    Button unicornButton;       // main UNICORN button
    Boolean unicorn_mode = false;           // TRUE if the UNICORN buttons are visible
    SupportMapFragment mapFragment;
    GoogleMap map;                  // the map fragment object
    ViewGroup mapParent = null;    // TODO - not entirely sure this is good enough!
    private AddressResultReceiver mResultReceiver;  // this is the receiver for the FetchAddress intent

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the main view
        setContentView(R.layout.activity_main);

        // Get the connection to the Google API sorted - for getting locations/address
        buildGoogleApiClient();
        // and ensure we have permissions for the actual calls later on...
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onCreate - NO PERMISSIONS - Requesting...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

        // Get the ID of the main button view, and the unicorn scrollable button view
        mainButtons = findViewById(R.id.main_buttons);
        unicornButtons = findViewById(R.id.unicorn_buttons);

        // Put some funky coloured text in the UNICORN button
        unicornButton = (Button) findViewById(R.id.button_unicorn);
        Spanned result = getSpannedHtml(getString(R.string.unicorn_button_text));
        unicornButton.setText(result);

        // Ensure the FetchAddress service has something to return result to
        mResultReceiver = new AddressResultReceiver(new Handler());

        Log.i(TAG, "...done onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume");
        // ALWAYS called after onCreate, so see if we have the Map Fragment sorted
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                Log.i(TAG, "onMapReady");
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(0, 0))
                        .title("Center of the World")
                        .snippet("Latitude 0 / Longitude 0")).showInfoWindow();
                setupMap(map);
            }
        });



    } // onResume

    private Spanned getSpannedHtml(String string) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(string);
        }
    }


    public void setupMap (GoogleMap googleMap) {
        Log.i(TAG, "setupMap");
        // Check for permissions, request if not there...
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "setupMap - NO PERMISSIONS - Requesting...");
            Toast.makeText(getApplicationContext(), "Location Permission Required", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        googleMap.setMyLocationEnabled(true);       // Sets my-location button - Needs the permissions
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);    // Sets the map type to be "hybrid"
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);    // Set the map toolbar to be enabled
        googleMap.getUiSettings().setMapToolbarEnabled(false);
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
                View v = getLayoutInflater().inflate(R.layout.map_marker, mapParent, false);
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

        // Set a listener on an infowindow, such that clicking it hides it again
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.v(TAG, "Hiding marker InfoWindow: " + marker.getTitle());
                marker.hideInfoWindow();

            }
        });
    }


    public void onStartUnicorns(View v) {
        Log.i(TAG, "...onStartUnicorns");
        // Set up all the unicorn names, etc.
        createUnicornArray();

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
        Log.i(TAG, "...onStartSpecial");

        // Construct a CameraPosition and move there...
        // zoom level 0 is the whole world, the higher the number, the more detailed/closer view
        int zoom_level = (int) ((Math.random() * 10) + 5);
        Log.i(TAG, "...zoom level will be " + zoom_level);

        LatLng everest = new LatLng(27.986065, 86.922623);

        map.addMarker(new MarkerOptions()
                .position(everest)
                .title("Top of the World")
                .snippet("Mount Everest")).showInfoWindow();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(everest)
                .zoom(zoom_level)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mLastLocation = new Location("");
        mLastLocation.setLatitude(everest.latitude);
        mLastLocation.setLongitude(everest.longitude);
        // Only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }


    }


    public void onUnicornButton(View v) {
        Log.i(TAG, "onUnicornButton");
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

        // Construct a CameraPosition and move there...
        // zoom level 0 is the whole world, the higher the number, the more detailed/closer view
        int zoom_level = (int) ((Math.random() * 10) + 5);
        Log.i(TAG, "...zoom level will be " + zoom_level);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(unicorns[i].getLocation())
                .zoom(zoom_level)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mLastLocation = new Location("");
        mLastLocation.setLatitude(unicorns[i].getLocation().latitude);
        mLastLocation.setLongitude(unicorns[i].getLocation().longitude);
        // Only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }

    }

    protected void startIntentService() {

        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    public void onStartMyself(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onStartMyself - NO PERMISSIONS - Requesting...");
            Toast.makeText(getApplicationContext(), "Location Permission Required", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        Toast.makeText(getApplicationContext(), "Trying to locate you...", Toast.LENGTH_SHORT).show();
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

        // Only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }

    }

    // Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.v(TAG, "onRequestPermissionResult... ");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG, "onRequestPermissionResult...LOCATION=GRANTED ");
                } else {
                    Log.v(TAG, "onRequestPermissionResult...LOCATION=DENIED ");
                }
            }
        }
    }

    // Runs when a GoogleApiClient object successfully connects.
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API: onConnected...");
    }

    // Runs when a GoogleApiClient object fails to connect
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        int err = result.getErrorCode();
        Log.i(TAG, "API: Connection failed: ConnectionResult.getErrorCode() = " + err);
    }

    // Runs if the connection to GoogleApiClient drops because of an error
    public void onDisconnected() {
        Log.i(TAG, "API: Disconnected");
    }

    // Runs if connection lost for some reason, attempts to re-establish the connection
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "API: Connection suspended");
        mGoogleApiClient.connect();
    }

    // Rely on system calling onSaveInstanceState when stopping to save the key variables
    // alongside the other 'bundle' instance state.
    // Then as system calls onRestoreInstanceState() only if there is a saved state to restore
    // (eg if changing screen orientation) then we pull the variables back from the bundle
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current UNICORN_MODE state
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

        //Define and populate the Unicorn array
        unicorns = new Unicorn[9];

        Log.i(TAG, "...starting to populate unicorn array");
        unicorns[0] = new Unicorn("ZERO", "ZERO", R.drawable.ic_launcher);
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
        Log.i(TAG, "...completed populating unicorn array");
    }

    class AddressResultReceiver extends ResultReceiver {
        private AddressResultReceiver(Handler handler) {
            super(handler);
        }

        protected void onReceiveResult(int resultCode, Bundle resultData) {

            String mAddressOutput;
            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.v(TAG, "AddressOutput is " + mAddressOutput);

            if (unicorn_mode) {
                if (resultCode == Constants.SUCCESS_RESULT) {
                    Toast.makeText(getApplicationContext(), "Unicorn has been found in " + mAddressOutput, Toast.LENGTH_SHORT).show();
                }
                if (resultCode == Constants.FAILURE_RESULT
                        && mAddressOutput.equals(getResources().getString(R.string.no_address_found))) {
                    Toast.makeText(getApplicationContext(), "Looks like this Unicorn is flying over water!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Show a toast message if an address was found.
                if (resultCode == Constants.SUCCESS_RESULT) {
                    Toast.makeText(getApplicationContext(), "You're in " + mAddressOutput, Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

}
