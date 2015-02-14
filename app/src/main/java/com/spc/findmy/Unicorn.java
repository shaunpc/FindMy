package com.spc.findmy;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Class UNICORN - Created by shaun on 07/02/15.
 */
public class Unicorn {

    private String name;
    private String desc;
    private int image;

    private LatLng location;
    private Marker marker;

    public Unicorn(String n, String d, int i) {
        // create the basic unicorn object, but don't set markers/locations yet
        name = n;
        desc = d;
        image = i;
        location = null;
        marker = null;
    }

    public void relocate(GoogleMap map) {
        // create a new latitude/longitude reference
        // Latitude, in degrees. This value is in the range [-90, 90].
        // Longitude, in degrees. This value is in the range [-180, 180).
        double latitude = (Math.random() * 181) - 90;
        double longitude = (Math.random() * 361) - 180;
        this.location = new LatLng(latitude, longitude);

        //create a marker on the map
        this.marker = map.addMarker(new MarkerOptions()
                .position(this.location)
                .title(this.name)
                .snippet(this.desc)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(this.image)));
    }

    public LatLng getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public Marker getMarker() {
        return this.marker;
    }

}