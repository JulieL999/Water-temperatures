package com.example.watertemperatures

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class GeocoderClass {
    // TODO: check if it work at all??
    fun geocode(coordinates: LatLng, context: Context) : String{
        /*
        val gc = Geocoder(context, Locale.getDefault())
        val names = gc.getFromLocation(coordinates.latitude, coordinates.longitude, 3)
        val name = names?.get(0)
        return name.toString()

         */
        var name: String = ""
        val geocodeListener = Geocoder.GeocodeListener { addresses ->
            // do something with the addresses list
            name = addresses[0].toString()
            Log.d("GEOCODER", "Geocode listener works! Locality: $name")

        }

        val geocoder = Geocoder(context)
        if (Build.VERSION.SDK_INT >= 33) {
            // declare here the geocodeListener, as it requires Android API 33
            geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1, geocodeListener)
        } else {
            val addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
            // TODO: delete old temperatures from the text view!
            // For Android SDK < 33, the addresses list will be still obtained from the getFromLocation() method
            val address = addresses?.get(0)
            return address.toString()

        }
        return name
    }

    fun reverseGeocode(lakeName: String, context: Context) : LatLng? {
        val gc = Geocoder(context, Locale.getDefault())
        val addresses = gc.getFromLocationName(lakeName, 1)
        if ((addresses == null) || (addresses.size == 0))
        {
            val toast = Toast.makeText(
                context,
                "Sorry! The provided name does not exist!",
                Toast.LENGTH_LONG
            )
            toast.show()
            return null
        }
        else
        {
            val address = addresses[0]
            val lat = address.latitude
            val lng = address.longitude
            val toast = Toast.makeText(context, "Lat:${lat}, Long:${lng}", Toast.LENGTH_LONG)
            toast.show()
            return LatLng(lat, lng)
        }
    }
}