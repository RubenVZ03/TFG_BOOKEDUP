package com.rubenvelasco.bookedup;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {

    public static LatLng getLocationFromAddress(Context context, String address) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("LocationHelper", "Latitude: " + latitude + ", Longitude: " + longitude);
                return new LatLng(latitude, longitude);
            } else {
                Log.d("LocationHelper", "No se encontraron resultados para la dirección: " + address);
                return null;
            }
        } catch (IOException e) {
            Log.e("LocationHelper", "Error al obtener la ubicación desde la dirección", e);
            return null;
        }
    }
}