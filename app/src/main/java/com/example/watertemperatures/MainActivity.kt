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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
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

    suspend fun databaseAcess(){
        val db = Room.databaseBuilder(applicationContext, CoordinateDatabase::class.java,"Coordinates")
            .build()
        val coordinateDao = db.coordinateDAO()

//        coordinateDao.insertAll(
//            Coordinate(1,"Wörthersee","46.62675465146807","14.136299099681358")
//        )

        val crd: List<Coordinate> = coordinateDao.getByIds(intArrayOf(1))
        for(c in crd){
            Log.d("Room","${c.cid} ${c.name} ${c.latitude} ${c.longitude}")
        }
    }


}