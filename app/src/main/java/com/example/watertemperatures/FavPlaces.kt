package com.example.watertemperatures

import CustomAdapter
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var addBtn: FloatingActionButton
    private lateinit var db: CoordinateDatabase
    private lateinit var coordinateDAO: CoordinateDAO
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_places)

        var temp : Double
        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()

        // check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 111)
        }

        // get list of lakes from DB
        var listOfLakes : List<Coordinate>

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            withContext(Dispatchers.IO) {
                db = Room.databaseBuilder(applicationContext, CoordinateDatabase::class.java,"Coordinates")
                    .build()
                coordinateDAO = db.coordinateDAO()
                listOfLakes = coordinateDAO.getAll()
            }
            for (lake in listOfLakes) {
                //val smiley = chooseSmile(lake.waterTemp)
                // TODO:choose appropriate smile later
                data.add(ItemsViewModel(lake.name, lake.waterTemp.toString(), R.drawable.smile1))
            }
        }

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data)

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter

        // TODO:add new place btn - think about floating button later
        val addCardView = findViewById<CardView>(R.id.add)
        addCardView.setOnClickListener {
            // TODO: appropriate colour and design for the popup window
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_input_layout, null)
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            popupWindow.isFocusable = true
            popupWindow.showAtLocation(it, Gravity.CENTER, 0, 0)

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

                    // fetch the actual temperature for the lake
                    // fetch temperature from API!
                    // https://api.tomorrow.io/v4/weather/forecast?location=42.3478,-71.0466&apikey=fdyRHqGggkm6gI4nM4LX6M89sobGS63N'
                    // https://api.stormglass.io/v2/weather/point?lat=${lat}&lng=${lng}&params=${params}


                    val startInstant = Instant.now() // Replace this with your actual start time
                    val endInstant = Instant.now() // Replace this with your actual end time

                    val params = "waterTemperature"
                    val queryParams = mapOf("lat" to lat.toString(), "lng" to lng.toString(), "params" to params,
                        "start" to startInstant.toEpochMilli().toString(), // Convert to UTC timestamp
                        "end" to endInstant.toEpochMilli().toString()) // Convert to UTC timestamp)
                    var url: String = buildUrl(
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

    }

    fun chooseSmile(temp: Double) : Int {
        if (temp < 10.0) {
            return 3
        }
        else if (temp < 16.0) {
            return 2
        }
        else if (temp < 21.0) {
            return 4
        }
        else {
            return 1
        }
    }

    suspend fun makeAPICall(url: String): String? {
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
        val temp: Double = 0.0
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
                //val scope = CoroutineScope(Dispatchers.Default)
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

                scope.launch {
                    withContext(Dispatchers.Main) {
                        //val smileNr = chooseSmile(temp)
                        data.add(ItemsViewModel(coord.name, temp.toString(), R.drawable.smile1))
                        adapter.notifyDataSetChanged()
                    }


                }


            }
        }





    }

    fun buildUrl(baseUrl: String, path: String, queryParams: Map<String, String>): String {
        val urlBuilder = StringBuilder(baseUrl)

        // Append the path to the URL
        if (!path.startsWith("/")) {
            urlBuilder.append("/")
        }
        urlBuilder.append(path)

        // Append query parameters to the URL
        if (queryParams.isNotEmpty()) {
            urlBuilder.append("?")
            queryParams.forEach { (key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&")
            }
            urlBuilder.deleteCharAt(urlBuilder.length - 1)
        }

        return urlBuilder.toString()
    }
}