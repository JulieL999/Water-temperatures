// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.watertemperatures

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.annotation.SuppressLint
import android.location.Location
import android.os.PersistableBundle
import android.util.Log
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.Math.abs

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
// [START maps_marker_on_map_ready]
class ClosestPlaces : AppCompatActivity(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var lastKnownLocation: Location? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var db: CoordinateDatabase
    private lateinit var coordinateDAO: CoordinateDAO
    private var locationPermissionGranted = false
    private var defaultLocation = LatLng(46.616223, 14.264396)

    private lateinit var coordinates: List<Coordinate>
    private lateinit var json: JSONObject
    val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
        throwable.printStackTrace()
    }

    private lateinit var listView: ListView
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){isGranted:Boolean ->
            if(isGranted){
                Log.i("Permission","Granted")
                getLocationPermission()
                getDeviceLocation()
            } else {
                Log.i("Permission","Denied")
            }
        }

    // [START_EXCLUDE]
    // [START maps_marker_get_map_async]
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps)
        listView=findViewById(R.id.listView)

        //Fetch Database information
//        runBlocking {
//            fetchDatabase()
//        }

        Places.initialize(applicationContext, getString(R.string.maps_api_key))
        placesClient = Places.createClient(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
    }

    // [START maps_marker_on_map_ready_add_marker]
    override fun onMapReady(map: GoogleMap) {
        this.map = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        getLocationPermission()

        updateLocationUI()

//        getDeviceLocation()
    }

    private fun getLocationPermission(){
        if(ContextCompat.checkSelfPermission(this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            locationPermissionGranted = true
        } else{
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
//            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
//            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when(requestCode){
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                //getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation(){
        try{
            if(locationPermissionGranted){
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this){task ->
                    if(task.isSuccessful){
                        lastKnownLocation = task.result
                        if(lastKnownLocation != null){
                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                            getNearbyPlaces()
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message,e)
        }
    }



    private fun addMarkers(googleMap: GoogleMap, list: List<Coordinate>){
        list.forEach{place ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .title(place.name)
                    .position(LatLng(place.latitude!!.toDouble(),place.longitude!!.toDouble()))
            )
        }
    }

  private  fun getNearbyPlaces(){
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            withContext(Dispatchers.IO+ coroutineExceptionHandler){
               makeApiCall { json ->
                   if(json == null){
                       Log.e("MyTag","Request is null!")
                   } else {
                       setCoordinates(json)
                   }
               }
            }
        }
    }

    private fun makeApiCall(callback: (JSONObject?) -> Unit): JSONObject {

        val key = getString(R.string.maps_api_key)
        val radius = 50000
        val request = Request.Builder()
            .url("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${lastKnownLocation!!.latitude},${lastKnownLocation!!.longitude}&radius=${radius}&language=en&keyword=lake&key=${key}")
            .build()

        val client = OkHttpClient()
        val response = OkHttpClient().newCall(request).execute().body!!.string()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val jResponse = response.body?.string()
                callback(JSONObject(jResponse))
            }
        })

        return JSONObject(response)



    }

   private fun setCoordinates(jsonObject: JSONObject){
        val results = jsonObject.getJSONArray("results")
       val list = mutableListOf<Coordinate>()
       val names = mutableListOf<String>()

        for(i in 0 until results.length()){
            val place = results.getJSONObject(i)
            val name = place.get("name")
            val location = place.getJSONObject("geometry").getJSONObject("location")
            val lat = location.get("lat")
            val lng = location.get("lng")
            val coordinate = Coordinate(abs((0..999999999).random()), name.toString(),lat.toString(),lng.toString(),0.0)
            list.add(coordinate)
            names.add(name.toString())
            Log.i("MyTag","Name: $name lat: $lat lng: $lng")
        }
       runOnUiThread{
           addMarkers(map!!,list)
       }

//       val adapter: ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_list_item_1,names)
//       listView.adapter=adapter

        }


    companion object{
        private val TAG = ClosestPlaces::class.java.simpleName
        private const val DEFAULT_ZOOM = 10
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        private const val KEY_CAMERA_POSITION = "camera position"
        private const val KEY_LOCATION = "location"

        private const val M_MAX_ENTRIES = 5
    }

}
// [END maps_marker_on_map_ready]