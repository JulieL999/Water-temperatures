package com.example.watertemperatures

import CustomAdapter
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.Instant
import java.util.Locale

class FavPlaces : AppCompatActivity() {
    // TODO: put back button
    private lateinit var db: CoordinateDatabase
    private lateinit var coordinateDAO: CoordinateDAO
    //TODO: check if the temperature is actual
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_places)

        val closeBtn = findViewById<Button>(R.id.closeBtn)
        closeBtn.setOnClickListener {
            intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }

        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)
        // ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()
        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data)
        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter

        // get list of lakes from DB
        var listOfLakes : List<Coordinate>
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            withContext(Dispatchers.Default) {
                db = Room.databaseBuilder(applicationContext, CoordinateDatabase::class.java,"Coordinates")
                    .build()
                coordinateDAO = db.coordinateDAO()
                listOfLakes = coordinateDAO.getAll()
            }
            withContext(Dispatchers.Main) {
                for (lake in listOfLakes) {
                    data.add(ItemsViewModel(lake.name, lake.waterTemp.toString(), chooseSmile(lake.waterTemp)))
                    Log.d("LAKE", "Name: ${lake.name}, Lat: ${lake.latitude}, Lng: ${lake.longitude}")
                    adapter.notifyDataSetChanged()
                }
            }
        }

        val addCardView = findViewById<CardView>(R.id.add)
        addCardView.setOnClickListener {
           inflatePopupWindow(it, data, adapter)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun inflatePopupWindow(view: View, data: ArrayList<ItemsViewModel>, adapter: CustomAdapter) {
        // check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 111)
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_input_layout, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        popupWindow.isFocusable = true
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        val submitButton = popupView.findViewById<Button>(R.id.addBtn)
        val inputEditText = popupView.findViewById<EditText>(R.id.nameOfTheLake)

        submitButton.setOnClickListener {
            val lakeName = inputEditText.text.toString()
            val gc = Geocoder(this, Locale.getDefault())

            var addresses = gc.getFromLocationName(lakeName, 1)
            if ((addresses == null) || (addresses.size == 0)) {
                val toast = Toast.makeText(applicationContext, "Sorry! The provided name does not exist!", Toast.LENGTH_LONG)
                toast.show()
            }
            else {
                val address = addresses[0]
                val lat = address.latitude
                val lng = address.longitude
                val toast = Toast.makeText(applicationContext, "Lat:${lat}, Long:${lng}", Toast.LENGTH_LONG)
                toast.show()

                // fetch temperature from API!
                // https://api.tomorrow.io/v4/weather/forecast?location=42.3478,-71.0466&apikey=fdyRHqGggkm6gI4nM4LX6M89sobGS63N'
                val startInstant = Instant.now() // Replace this with your actual start time
                val endInstant = Instant.now() // Replace this with your actual end time

                val params = "waterTemperature"
                val queryParams = mapOf("lat" to lat.toString(), "lng" to lng.toString(), "params" to params,
                    "start" to startInstant.toEpochMilli().toString(), // Convert to UTC timestamp
                    "end" to endInstant.toEpochMilli().toString()) // Convert to UTC timestamp)
                val urlBuilder = URLBuilder()
                var url: String = urlBuilder.buildUrl(
                    "https://api.stormglass.io",
                    "/v2/weather/point",
                    queryParams
                )
                var newCoordinate = Coordinate(0, lakeName, lat.toString(), lng.toString(), 0.0)
                getActualWaterTemperatures(url, newCoordinate, data, adapter)
                popupWindow.dismiss()
            }
        }
    }

    fun chooseSmile(temp: Double) : Int {
        return if (temp < 10.0) {
            R.drawable.smile3
        } else if (temp < 16.0) {
            R.drawable.smile2
        } else if (temp < 21.0) {
            R.drawable.smile4
        } else {
            R.drawable.smile1
        }
    }

    fun makeAPICall(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader(
                "Authorization",
                "9ddde4f8-5fbb-11ee-a654-0242ac130002-9ddde55c-5fbb-11ee-a654-0242ac130002"
            )
            .get()
            .build()

        val responseBody = client.newCall(request).execute().body
        var gsonString = responseBody!!.string()

        Log.i("RESPONSE", gsonString)
        return gsonString
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getActualWaterTemperatures(
        url: String,
        coord: Coordinate,
        data: ArrayList<ItemsViewModel>,
        adapter: CustomAdapter
    ) {
        val scope = CoroutineScope(Dispatchers.Default)
        val result = scope.async {
            // Execute some code asynchronously
            withContext(Dispatchers.IO) {
                val resp = makeAPICall(url)
                if (resp != null) {
                    JSONObject(resp).getJSONArray("hours").getJSONObject(0).getJSONObject("waterTemperature").getDouble("sg")
                } else {
                    null
                }
            }
        }
        result.invokeOnCompletion {
            if (it == null) {
                var temp = result.getCompleted()
                if (temp == null) {
                    temp = 0.0
                }
                Log.d("WATER_TEMP", temp.toString())
                //add to database
                scope.launch {
                    coordinateDAO.insertAll(
                        Coordinate(
                            0,
                            coord.name,
                            coord.latitude,
                            coord.longitude,
                            temp
                        )
                    )
                }

                // update UI
                scope.launch {
                    withContext(Dispatchers.Main) {
                        val smileNr = chooseSmile(temp)
                        data.add(ItemsViewModel(coord.name, temp.toString(), smileNr))
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}