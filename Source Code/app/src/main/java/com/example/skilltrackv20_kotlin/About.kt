package com.example.skilltrackv20_kotlin
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView

class About : AppCompatActivity() {

    // Variables
    private lateinit var imgBackground: ImageView
    private lateinit var imgView: ImageView
    private lateinit var imgView2: ImageView
    private lateinit var imgView3: ImageView
    private lateinit var cv1: RelativeLayout
    private lateinit var cv2: RelativeLayout
    private lateinit var cv3: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Type cast
        cv1 = findViewById(R.id.rlNoData)
        cv2 = findViewById(R.id.rlNoData2)
        cv3 = findViewById(R.id.rlNoData3)

        // Type cast the image view
        imgBackground = findViewById(R.id.imgBackground)
        imgView = findViewById(R.id.imgView2)
        imgView2 = findViewById(R.id.imgView3)
        imgView3 = findViewById(R.id.imgView4)

        // Use Glide to animate image with gif
        Glide.with(this).asGif().load(R.drawable.bg3).into(imgBackground)
        Glide.with(this).asGif().load(R.drawable.gotodash).into(imgView)
        Glide.with(this).asGif().load(R.drawable.gotocat).into(imgView2)
        Glide.with(this).asGif().load(R.drawable.gototimesheet).into(imgView3)

        // OnClicks
        cv1.setOnClickListener {
            startActivity(Intent(applicationContext, Dashboard::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        cv2.setOnClickListener {
            startActivity(Intent(applicationContext, CategoryActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        cv3.setOnClickListener {
            startActivity(Intent(applicationContext, TimesheetActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Setup bottom NavBar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_About

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_About -> true
                R.id.bottom_Dashboard -> {
                    startActivity(Intent(applicationContext, Dashboard::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_Profile -> {
                    startActivity(Intent(applicationContext, Profile::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    fun GotoDash() {
        // Method implementation
    }

    fun GotoCategory() {
        startActivity(Intent(applicationContext, CategoryActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    fun GotoTimesheet() {
        startActivity(Intent(applicationContext, TimesheetActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}
