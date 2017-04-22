package com.spc.findmy;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("MapsActivity","onResume");
        // ALWAYS called after onCreate, so see if we have the Map Fragment sorted
        SupportMapFragment mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mMap != null) {
            // So, get the map and define call-back to add first marker when ready
            mMap.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
                }
            });
        }
    } // onResume

}

