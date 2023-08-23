package com.example.skilltrackv20_kotlin

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class CategoryActivity : AppCompatActivity() {

    private lateinit var tvHeader: TextView
    private lateinit var etCategoryName: EditText
    private lateinit var etCategoryMax: EditText
    private lateinit var etCategoryMin: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnUpload: Button
    private lateinit var imgBackground: ImageView
    private lateinit var imgView: ImageView

    private var filePath: Uri? = null
    private var filePathFS: String? = null
    private val IMAGE_SELECTOR_REQUEST_CODE = 1

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        tvHeader = findViewById(R.id.tvHeader)
        etCategoryName = findViewById(R.id.etCategory)
        etCategoryMax = findViewById(R.id.etCategoryMax)
        etCategoryMin = findViewById(R.id.etCategoryMin)
        btnSubmit = findViewById(R.id.btnSubmit)

        imgBackground = findViewById(R.id.imgBackground)
        Glide.with(this).asGif().load(R.drawable.bg4).into(imgBackground)

        btnSubmit.setOnClickListener { view: View ->
            addCategory(view)
        }

        btnUpload = findViewById(R.id.btnChoose)
        imgView = findViewById(R.id.imgView)

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        btnUpload.setOnClickListener { v: View ->
            selectImage()
        }

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

    private fun addCategory(view: View) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        if (etCategoryName.text.toString().isNotEmpty()) {
            db.collection("Users/${user!!.uid}/Categories")
                .document(etCategoryName.text.toString())
                .get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (task.isSuccessful) {
                        val document: DocumentSnapshot? = task.result
                        if (document != null && document.exists()) {
                            Log.d(TAG, "Category already exists")
                            etCategoryName.error = "Category already exists"
                        } else {
                            if (etCategoryMax.text.toString().isNotEmpty() && etCategoryMin.text.toString().isNotEmpty()) {
                                uploadCategory()
                            } else {
                                etCategoryMax.error = "Cannot be blank"
                                etCategoryMin.error = "Cannot be blank"
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.exception)
                    }
                }
        } else {
            etCategoryName.error = "Please enter a category name"
        }
    }

    private fun selectImage() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
        startActivityForResult(chooserIntent, IMAGE_SELECTOR_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_SELECTOR_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap: Bitmap? = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                Glide.with(this).load(filePath).into(imgView)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadCategory() {
        if (filePath != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val ref: StorageReference = storageReference.child("Images/${UUID.randomUUID()}")

            val uploadTask: UploadTask = ref.putFile(filePath!!)
            uploadTask.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                filePathFS = taskSnapshot.metadata!!.reference!!.name

                Log.d(TAG, "Category doesn't exist, Creating new...")

                val db = FirebaseFirestore.getInstance()
                val user = FirebaseAuth.getInstance().currentUser

                val categoryObject: MutableMap<String, Any> = HashMap()
                categoryObject["CategoryName"] = etCategoryName.text.toString()
                categoryObject["CategoryMin"] = etCategoryMin.text.toString()
                categoryObject["CategoryMax"] = etCategoryMax.text.toString()
                categoryObject["ImagePath"] = filePathFS.toString()

                db.collection("Users/${user!!.uid}/Categories")
                    .document(etCategoryName.text.toString())
                    .set(categoryObject)
                    .addOnSuccessListener { aVoid: Void? ->
                        Log.d(TAG, "DocumentSnapshot successfully written")
                    }
                    .addOnFailureListener { e: Exception ->
                        Log.w(TAG, "Error adding document", e)
                    }

                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Category uploaded successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(applicationContext, Dashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
                .addOnFailureListener { e: Exception ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Failed " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    progressDialog.setMessage("Uploaded ${(progress.toInt())}%")
                }
        } else {
            btnUpload.error = "Please upload an image first"
            Toast.makeText(
                this,
                "Please upload an image first",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
