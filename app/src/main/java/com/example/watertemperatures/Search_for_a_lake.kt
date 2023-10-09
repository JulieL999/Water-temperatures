package com.example.watertemperatures

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.watertemperatures.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Locale

class SearchForaLake : AppCompatActivity(), OnMapReadyCallback {
    // TODO : think about to put a place in the database with its temperature
    private lateinit var map: GoogleMap
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_for_alake)

        // check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 111)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val findBtn = findViewById<Button>(R.id.button)
        val input = findViewById<EditText>(R.id.inputField)
        findBtn.setOnClickListener {
            val lakeName = input.text.toString()
            val helper : HelperClassForGettingTemperatures = HelperClassForGettingTemperatures()
            val geocoder : GeocoderClass = GeocoderClass()
            val url: String
            val coordinates: LatLng? = geocoder.reverseGeocode(lakeName, this)

            if (coordinates != null) {
                Log.d("COORDINATES", "${coordinates.latitude}, ${coordinates.longitude}")
                val localityField = findViewById<TextView>(R.id.locality)
                val locality = "Locality: $lakeName"
                localityField.text = locality
                url = helper.prepareAPICall(coordinates)
                Log.d("URL", url)
                var temp : Double
                val scope = CoroutineScope(Dispatchers.Default)
                scope.launch {
                    withContext(Dispatchers.IO) {
                        temp = helper.getActualWaterTemperatures(url)
                    }
                    withContext(Dispatchers.Main) {
                        val tempField = findViewById<TextView>(R.id.temperature)
                        val newValue = "Temperature: $temp"
                        tempField.text = newValue
                    }
                }
                    // put marker in the correct place!!
                    val latLng = LatLng(coordinates.latitude, coordinates.longitude)
                    map.addMarker(MarkerOptions().position(latLng).title(lakeName))
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    Toast.makeText(applicationContext, coordinates.latitude.toString() + " " + coordinates.longitude, Toast.LENGTH_LONG).show()

            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        val Worthersee = LatLng(46.626227,14.148225)
        map.addMarker(MarkerOptions()
            .position(Worthersee)
            .title("Marker in Worthersee"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(Worthersee,10f))
        map.uiSettings.isZoomControlsEnabled = true

        map.setOnMapClickListener { latLng ->
            // Clears the previously touched position
            map.clear();
            // Animating to the touched position
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            val location = LatLng(latLng.latitude, latLng.longitude)
            map.addMarker(MarkerOptions().position(location))
            map.uiSettings.isZoomControlsEnabled = true

            val geocoding = GeocoderClass()
            val name = geocoding.geocode(latLng, this)
            val localityField = findViewById<TextView>(R.id.locality)
            val locality = "Locality: $name"
            localityField.text = locality
            // TODO: show temperature
        }
    }

}