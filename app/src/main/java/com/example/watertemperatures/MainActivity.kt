package com.example.watertemperatures

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.constraintlayout.motion.widget.MotionLayout

class MainActivity : AppCompatActivity(){

    private lateinit var motionLayout: MotionLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        motionLayout = findViewById(R.id.motionLayout)

        val btnFavPlaces = findViewById<Button>(R.id.btnFavouritePlaces)
        btnFavPlaces.setOnClickListener {
            intent = Intent(applicationContext, FavPlaces::class.java)
            startActivity(intent)
        }

        val btnAboutPage = findViewById<Button>(R.id.btnAbout)
        btnAboutPage.setOnClickListener {
            intent = Intent(applicationContext, AboutPage::class.java)
            startActivity(intent)
        }

        val btnSearch = findViewById<Button>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            intent = Intent(applicationContext, SearchForaLake::class.java)
            startActivity(intent)
        }

        val btnClosestPlaces = findViewById<Button>(R.id.btnClosestPlaces)
        btnClosestPlaces.setOnClickListener {
            intent = Intent(applicationContext, ClosestPlaces::class.java)
            startActivity(intent)
        }

    }


}