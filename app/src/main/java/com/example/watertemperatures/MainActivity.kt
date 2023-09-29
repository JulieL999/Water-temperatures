package com.example.watertemperatures

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity(){

    private lateinit var db: CoordinateDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Code for acessing coordinate database
        runBlocking {
            databaseAcess()
        }


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

        val bubble1 = findViewById<ImageView>(R.id.bubble1)
        val bubble2 = findViewById<ImageView>(R.id.bubble2)
        val bubble3 = findViewById<ImageView>(R.id.bubble3)
        val bubble4 = findViewById<ImageView>(R.id.bubble4)
        val bubble5 = findViewById<ImageView>(R.id.bubble5)
        val fish = findViewById<ImageView>(R.id.fish)

        val goUntil = 500f

        val bubbleAnim1 = ObjectAnimator.ofFloat(bubble1, "translationY", -goUntil).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            startDelay = 2000
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
        }

    val bubbleAnim2 = ObjectAnimator.ofFloat(bubble2, "translationY", -goUntil).apply {
        duration = 3000
        repeatCount = ObjectAnimator.INFINITE
        startDelay = 2000
        repeatMode = ObjectAnimator.RESTART
        interpolator = LinearInterpolator()
    }

    // used for setting entries into database




        val bubbleAnim3 = ObjectAnimator.ofFloat(bubble3, "translationY", -goUntil).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            startDelay = 3000
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
        }

        val bubbleAnim4 = ObjectAnimator.ofFloat(bubble4, "translationY", -goUntil).apply {
            duration = 4000
            repeatCount = ObjectAnimator.INFINITE
            startDelay = 2000
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
        }

        val bubbleAnim5 = ObjectAnimator.ofFloat(bubble5, "translationY", -goUntil).apply {
            duration = 4000
            repeatCount = ObjectAnimator.INFINITE
            startDelay = 2000
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
        }

        val fishAnim = ObjectAnimator.ofFloat(fish, "translationX", 1000f).apply {
            duration = 10000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
        }

        val animations = listOf(bubbleAnim1, bubbleAnim2, bubbleAnim3, bubbleAnim4, bubbleAnim5, fishAnim)
        val animatorSet = AnimatorSet().apply {
            for (animation in animations) {
                playTogether(animation)
            }
        }

        animatorSet.start()

    }
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