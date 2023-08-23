package com.example.skilltrackv20_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class Dashboard : AppCompatActivity(), CategoryAdapter.ICategoryDetails {
    private lateinit var recyclerView: RecyclerView
    private lateinit var imgBackground: ImageView
    private lateinit var imgView2: ImageView
    private lateinit var categoryArrayList: ArrayList<Category>
    private lateinit var rlNoData: RelativeLayout
    private lateinit var fab: FloatingActionButton
    private lateinit var cvNoData: CardView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var progressDialog: ProgressDialog
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Type cast the image view
        imgBackground = findViewById(R.id.imgBackground)
        imgView2 = findViewById(R.id.imgView2)

        // Use Glide to animate image with gif
        Glide.with(this).asGif().load(R.drawable.bg5).into(imgBackground)
        Glide.with(this).asGif().load(R.drawable.nocat).into(imgView2)

        rlNoData = findViewById(R.id.rlNoData)
        cvNoData = findViewById(R.id.cvNoData)
        emptyText = findViewById(R.id.tv_no_data)

        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser!!
        categoryArrayList = ArrayList()
        categoryAdapter = CategoryAdapter(this, categoryArrayList, this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = categoryAdapter

        fab = findViewById(R.id.fabCategory)
        fab.setOnClickListener { view: View ->
            startActivity(Intent(applicationContext, CategoryActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading...")
        progressDialog.show()
        eventChangeListener()

        // Setup bottom NavBar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.bottom_Dashboard

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_About -> {
                    startActivity(Intent(applicationContext, About::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                R.id.bottom_Dashboard -> {
                    startActivity(Intent(applicationContext, About::class.java))
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

    private fun eventChangeListener() {
        db.collection("Users/${user.uid}/Categories").get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        for (document in task.result!!) {
                            categoryArrayList.add(document.toObject(Category::class.java))
                            cvNoData.visibility = View.INVISIBLE

                            if (categoryArrayList.size < 1) {
                                categoryArrayList.clear()
                                categoryAdapter.notifyDataSetChanged()
                                cvNoData.visibility = View.VISIBLE

                                if (progressDialog.isShowing) {
                                    progressDialog.dismiss()
                                }
                            }
                        }
                    }

                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                        categoryAdapter.notifyDataSetChanged()
                    }
                }

                if (!task.isSuccessful) {
                    if (progressDialog.isShowing) {
                        progressDialog.setCancelable(true)
                        progressDialog.setMessage("Error loading data \n Please check your internet connection")
                        return@addOnCompleteListener
                    }
                }
            }
    }

    override fun GotoCategoryDetails(category: Category) {
        val intent = Intent(this, CategoryDetails::class.java)
        intent.putExtra("object", category.CategoryName)
        startActivity(intent)
    }

}
