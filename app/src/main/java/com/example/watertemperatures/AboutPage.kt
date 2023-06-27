package com.example.watertemperatures

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.utils.widget.ImageFilterView

class AboutPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val closeBtn = findViewById<Button>(R.id.close_btn)
        closeBtn.setOnClickListener {
            intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }

        val facebook = findViewById<ImageView>(R.id.logo_f)
        facebook.setOnClickListener{
            intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("https://www.facebook.com/julia.lomonosova.50"))
            startActivity(intent)
        }

        val instagram = findViewById<ImageView>(R.id.logo_insta)
        instagram.setOnClickListener {
            intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("https://www.instagram.com/julia.traveller/"))
            startActivity(intent)
        }


    }
}