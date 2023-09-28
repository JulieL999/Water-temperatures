package com.example.watertemperatures

import CustomAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import org.json.JSONObject

class FavPlaces : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_places)

        // fetch temperature from API!

        /*
            val lat = 58.7984
            val lng = 17.8081
            val params = "windSpeed"
            val apiKey = "example-api-key"

            val url = "https://api.stormglass.io/v2/weather/point?lat=$lat&lng=$lng&params=$params"

            val headers = mapOf("Authorization" to apiKey)

        //val ipAddress = get(url = "http://httpbin.org/ip").jsonObject.getString("origin")

        //val response = get(url, headers = headers)

            if (response.statusCode == 200) {
                val jsonData = JSONObject(response.text)
                // Do something with the JSON data
            } else {
                // Handle the error
                println("Error: ${response.statusCode} - ${response.text}")
            }

         */


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
}