package com.example.skilltrackv20_kotlin

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout

import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class Profile : AppCompatActivity() {

    private lateinit var imgBackground: ImageView
    private lateinit var imgView: ImageView
    private lateinit var rl: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        rl = findViewById(R.id.rlNoData)

        // Type cast the image view
        imgBackground = findViewById(R.id.imgBackground)
        imgView = findViewById(R.id.imgView)
        // Use Glide to animate image with GIF
        Glide.with(this).asGif().load(R.drawable.bg3).into(imgBackground)
        Glide.with(this).asGif().load(R.drawable.logout).into(imgView)

        // OnClicks
        rl.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(applicationContext, Login::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        // Setup bottom NavBar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_Profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_About -> {
                    startActivity(Intent(applicationContext, About::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_Dashboard -> {
                    startActivity(Intent(applicationContext, Dashboard::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_Profile -> true
                else -> false
            }
        }
    }
}
