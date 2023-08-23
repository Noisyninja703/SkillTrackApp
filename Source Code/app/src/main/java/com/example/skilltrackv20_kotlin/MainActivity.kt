package com.example.skilltrackv20_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.util.*

class MainActivity : AppCompatActivity() {

    // Variables
    private lateinit var imgLoading: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Type cast the image view
        imgLoading = findViewById(R.id.imgLoading)

        // Use Glide to animate image with gif
        Glide.with(this)
            .asGif()
            .load(R.drawable.stloader)
            .into(imgLoading)

       Timer().schedule(object : TimerTask() {
      override fun run() {
           startActivity(Intent(applicationContext, Main_Nav::class.java))
              finish()
           } }, 2150)
    }
}