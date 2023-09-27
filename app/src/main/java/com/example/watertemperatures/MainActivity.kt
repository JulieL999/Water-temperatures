package com.example.watertemperatures

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.room.Room
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity(){

    private lateinit var motionLayout: MotionLayout
    private lateinit var db: CoordinateDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Code for acessing coordinate database
        runBlocking {
            databaseAcess()
        }

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

    // used for setting entries into database
    suspend fun databaseAcess(){
        db = Room.databaseBuilder(applicationContext, CoordinateDatabase::class.java,"Coordinates")
            .build()
        val coordinateDao = db.coordinateDAO()

//        coordinateDao.insertAll(
//            Coordinate(1,"Worthersee","46.62727831226116","14.110936543942412"),
//            Coordinate(2,"Keutschachersee","46.58534725975028","14.159639373326728"),
//            Coordinate(3, "Maltschacersee","46.703241956065085","14.142326232846942"),
//            Coordinate(4,"Baßgeigensee","46.587253915810955","14.202405700047533"),
//            Coordinate(5,"Rauschelsee","46.58469136779188","14.220967113932259")
//        )

        val crd: List<Coordinate> = coordinateDao.getByIds(intArrayOf(1))
        for(c in crd){
            Log.d("Room","${c.cid} ${c.name} ${c.latitude} ${c.longitude}")
        }
    }



}