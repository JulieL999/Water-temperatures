package com.example.watertemperatures

import CustomAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class FavPlaces : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_places)

        // fetch temperature from API!
        // https://api.tomorrow.io/v4/weather/forecast?location=42.3478,-71.0466&apikey=fdyRHqGggkm6gI4nM4LX6M89sobGS63N'
        // https://api.stormglass.io/v2/weather/point?lat=${lat}&lng=${lng}&params=${params}
        val lat = 46.62013661540484
        val lng = 14.25296765628263
        val params = "waterTemperature"
        val queryParams = mapOf("lat" to lat.toString(), "lng" to lng.toString(), "params" to params)
        var url: String = buildUrl(
            "https://api.stormglass.io",
            "/v2/weather/point",
            queryParams
        )
        getActualWaterTemperatures("https://api.stormglass.io/v2/weather/point?lat=58.7984&lng=17.8081&params=waveHeight,waterTemperature")

        // --------------------------

        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()


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
            withContext(Dispatchers.IO) {
                val resp = makeAPICall(url)

                if (resp != null) {
                    Log.i("RESP", resp.toString())
                    val parsedObject = resp
                    //val temperature = parsedObject.getJSONObject("data").get("waterTemperature").toString()
                    //Log.i("TEMP", )
                } else {
                    Log.i("Response", "null :(")
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