package com.example.watertemperatures

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.watertemperatures.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SearchForaLake : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_for_alake)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }


    override fun onMapReady(p0: GoogleMap) {
        map = p0
        val Worthersee = LatLng(46.626227,14.148225)
        map.addMarker(MarkerOptions()
            .position(Worthersee)
            .title("Marker in Worthersee"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(Worthersee,10f))
    }
}