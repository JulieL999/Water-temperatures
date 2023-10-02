package com.example.watertemperatures

import CustomAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.time.Instant

class FavPlaces : AppCompatActivity() {
    private lateinit var db: CoordinateDatabase
    private lateinit var coordinateDAO: CoordinateDAO
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_places)

        var listOfLakes : List<Coordinate>
        //Code for acessing coordinate database
        runBlocking {
            db = Room.databaseBuilder(applicationContext, CoordinateDatabase::class.java,"Coordinates")
                .build()
            coordinateDAO = db.coordinateDAO()
            listOfLakes = coordinateDAO.getAll()
        }


        // fetch temperature from API!
        // https://api.tomorrow.io/v4/weather/forecast?location=42.3478,-71.0466&apikey=fdyRHqGggkm6gI4nM4LX6M89sobGS63N'
        // https://api.stormglass.io/v2/weather/point?lat=${lat}&lng=${lng}&params=${params}
        val lat = 46.62013661540484
        val lng = 14.25296765628263

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
        getActualWaterTemperatures(url)

        // --------------------------

        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()

        // new code
        for (lake in listOfLakes) {
            /*
            add appropriate smile!!!
            if (lake.waterTemp >= 20.0) {

            }

             */
            data.add(ItemsViewModel(lake.name, lake.waterTemp.toString(), R.drawable.smile1))
        }
        //

        data.add(ItemsViewModel("Wörthersee", "25°", R.drawable.smile1))
        data.add(ItemsViewModel("Some other see", "16°", R.drawable.smile2))
        data.add(ItemsViewModel("Some other see 2", "10°", R.drawable.smile3))
        data.add(ItemsViewModel("Some other see 3", "19°", R.drawable.smile4))

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data)

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter

        // --------------- add new place

        val addCardView = findViewById<CardView>(R.id.add)
        addCardView.setOnClickListener {
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
                data.add(ItemsViewModel(lakeName, "", R.drawable.smile1))
                popupWindow.dismiss()
            }
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

    fun getActualWaterTemperatures(url: String) {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            // Execute some code asynchronously
            val result = withContext(Dispatchers.IO) {
                val resp = makeAPICall(url)
                if (resp != null) {
                    JSONObject(resp).getJSONArray("hours").getJSONObject(0).getJSONObject("waterTemperature").getDouble("sg")
                } else {
                    "null"
                }

            }

            withContext(Dispatchers.Main) {
                val temp = findViewById<TextView>(R.id.tempText)
                temp.text = result.toString()
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

    suspend fun databaseAcess(){


        /*
      coordinateDao.insertAll(
          Coordinate(1,"Worthersee","46.62727831226116","14.110936543942412", 20.0),
          Coordinate(2,"Keutschachersee","46.58534725975028","14.159639373326728",20.0),
          Coordinate(3, "Maltschachersee","46.703241956065085","14.142326232846942",20.0),
          Coordinate(4,"Baßgeigensee","46.587253915810955","14.202405700047533",20.0),
          Coordinate(5,"Rauschelsee","46.58469136779188","14.220967113932259",20.0)
        )



        val crd: List<Coordinate> = coordinateDao.getByIds(intArrayOf(1))
        for(c in crd){
            Log.d("Room","${c.cid} ${c.name} ${c.latitude} ${c.longitude} ${c.waterTemp}")
        }

         */
    }
}