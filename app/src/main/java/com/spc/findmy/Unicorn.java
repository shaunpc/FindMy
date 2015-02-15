package com.spc.findmy;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Class UNICORN - Created by shaun on 07/02/15.
 */
public class Unicorn {

    protected static final String TAG = "Unicorn";

    private String name;
    private String desc;
    private int image;

    private LatLng location;
    private Marker marker;

    public Unicorn(String n, String d, int i) {
        // create the basic unicorn object, but don't set markers/locations yet
        Log.i(TAG, "...building Unicorn " + n);
        this.name = n;
        this.desc = d;
        this.image = i;
        this.location = null;
        this.marker = null;
    }

    public void relocate(GoogleMap map) {

        Log.i(TAG, "...relocating Unicorn " + this.name);
        // create a new latitude/longitude reference
        // Latitude, in degrees. This value is in the range [-90, 90].
        // Longitude, in degrees. This value is in the range [-180, 180).
        double latitude = (Math.random() * 181) - 90;
        double longitude = (Math.random() * 361) - 180;
        this.location = new LatLng(latitude, longitude);


        // create a marker on the map, or just update position
        if (this.marker == null) {
            Log.i(TAG, "Unicorn " + this.name + " set up at location " + this.location);
            this.marker = map.addMarker(new MarkerOptions()
                    .position(this.location)
                    .title(this.name)
                    .snippet(this.desc)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(this.image)));
        } else {
            Log.i(TAG, "Unicorn " + this.name + " moved to location " + this.location);
            this.marker.setPosition(this.location);
        }


    }

    public LatLng getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setDesc(String d) {
        this.desc = d;
    }

    public void setImage(int i) {
        this.image = i;
    }

    public Marker getMarker() {
        return this.marker;
    }

}