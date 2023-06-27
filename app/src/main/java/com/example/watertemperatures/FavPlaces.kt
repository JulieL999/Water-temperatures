package com.example.watertemperatures

import CustomAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavPlaces : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_places)

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
        data.add(ItemsViewModel("Add new place", "", R.drawable.baseline_add_24))

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data)

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter
    }
}