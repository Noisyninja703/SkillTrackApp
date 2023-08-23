package com.example.skilltrackv20_kotlin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker;
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import org.jetbrains.annotations.NotNull
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Month
import java.time.MonthDay
import java.time.Year
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TimesheetActivity : AppCompatActivity() {

    //Variables
    private lateinit var listView: ListView
    private lateinit var btnDate: Button
    private lateinit var btnStartTime: Button
    private lateinit var btnEndTime: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnUpload: Button
    private lateinit var etDescription: EditText
    private lateinit var etKills: EditText
    private lateinit var etDeaths: EditText
    private lateinit var etAssists: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var imgBackground: ImageView
    private lateinit var imgView: ImageView

    // Uri indicates, where the image will be picked from
    private var filePath: Uri? = null
    // Uri indicates, where the image will be downloaded from
    private var filePathFS: String? = null
    // request code
    private val IMAGE_SELECTOR_REQUEST_CODE = 1

    // instance for firebase storage and StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private var categories = ArrayList<String>()
    private val defaultCategoryIndex = 0 // Declare the variable for the default category index

    //Initialize calender and timepicker variables
    private val myCalendar = Calendar.getInstance()

    //Initialize variables for Timesheet Entry
    private var startTime: Date? = null
    private var endTime: Date? = null
    private var date: Date? = null
    private var description: String? = null
    private var category: String? = null
    private var kd = 0.0
    private var playtime = 0.0
    private var kills = 0
    private var deaths = 0
    private var assists = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheet)

        // Typecast
        btnDate = findViewById(R.id.startDateButton)
        btnStartTime = findViewById(R.id.startTimeButton)
        btnEndTime = findViewById(R.id.endTimeButton)
        btnSubmit = findViewById(R.id.submitButton)
        btnUpload = findViewById(R.id.btnChoose)
        etDescription = findViewById(R.id.descriptionEditText)
        etKills = findViewById(R.id.etKills)
        etDeaths = findViewById(R.id.etDeaths)
        etAssists = findViewById(R.id.etAssists)
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        imgBackground = findViewById(R.id.imgBackground)
        imgView = findViewById(R.id.imgView)
        Glide.with(this).asGif().load(R.drawable.bg1).into(imgBackground)

        // Button On Clicks
        // Date Picker
        val date =
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, day: Int ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = month
                myCalendar[Calendar.DAY_OF_MONTH] = day
                updateLabel()
            }
        btnDate.setOnClickListener {
            DatePickerDialog(
                this,
                date,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        // Start Time Picker
        val time =
            TimePickerDialog.OnTimeSetListener { _: TimePicker?, hour: Int, minute: Int ->
                myCalendar[Calendar.HOUR_OF_DAY] = hour
                myCalendar[Calendar.MINUTE] = minute
                updateStartLabel()
            }
        btnStartTime.setOnClickListener {
            TimePickerDialog(
                this,
                time,
                myCalendar[Calendar.HOUR],
                myCalendar[Calendar.MINUTE],
                true
            ).show()
        }

        // End Time Picker
        val endTime =
            TimePickerDialog.OnTimeSetListener { _: TimePicker?, hour: Int, minute: Int ->
                myCalendar[Calendar.HOUR_OF_DAY] = hour
                myCalendar[Calendar.MINUTE] = minute
                updateEndLabel()
            }
        btnEndTime.setOnClickListener {
            TimePickerDialog(
                this,
                endTime,
                myCalendar[Calendar.HOUR],
                myCalendar[Calendar.MINUTE],
                true
            ).show()
        }

        // Upload Image
        btnUpload.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
            startActivityForResult(chooserIntent, IMAGE_SELECTOR_REQUEST_CODE)
        }

        // Submit Button
        btnSubmit.setOnClickListener {
            captureValues()
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
//                    startActivity(Intent(applicationContext, Dashboard::class.java))
                    finish()
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

        // Setup the spinner
// Initialize Firebase objects
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        db.collection("Users/${user?.uid}/Categories").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val categories = ArrayList<String>()
                    for (document in task.result) {
                        categories.add(document.id)
                    }
                    populateSpinner(categories)
                } else {
                    val categories = ArrayList<String>()
                    categories.add("No Categories Found")
                    populateSpinner(categories)
                }
            }
    }

    private fun populateSpinner(categories: List<String>) {
        // Setup the spinner --> Add user's categories from Firestore
        val categorySpinner: Spinner = findViewById(R.id.categorySpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter
        categorySpinner.requestFocus()

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                (view as TextView).setTextColor(Color.WHITE) // Change selected text color
                category = parent.getItemAtPosition(position).toString()
                if (parent.getItemAtPosition(position).toString() == "No Categories Found") {
                    view.error = "Create a Category before creating a new Timesheet"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }


    private fun updateLabel() {
        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        btnDate.text = sdf.format(myCalendar.time)
    }

    private fun updateStartLabel() {
        val myFormat = "HH:mm"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        btnStartTime.text = sdf.format(myCalendar.time)
    }

    private fun updateEndLabel() {
        val myFormat = "HH:mm"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        btnEndTime.text = sdf.format(myCalendar.time)
    }

    private fun uploadImage() {
        if (filePath != null) {
            val ref = storageReference.child("images/" + UUID.randomUUID().toString())
            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    Toast.makeText(this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_SELECTOR_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            filePathFS = filePath!!.path!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imgView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun captureValues() {
        // Capture the input data to the Firestore db

        description = etDescription.text.toString()

        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

        val dateString = btnDate.text.toString()
        val startTimeString = btnStartTime.text.toString()
        val endTimeString = btnEndTime.text.toString()

        var date: Date? = null
        var startTime: Date? = null
        var endTime: Date? = null

        // Try to parse date/time data / catch errors
        try {
            date = dateFormat.parse(dateString)
            startTime = timeFormat.parse(startTimeString)
            endTime = timeFormat.parse(endTimeString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        // Try to parse user game stat data / catch errors
        try {
            kills = etKills.text.toString().toInt()
            deaths = etDeaths.text.toString().toInt()
            assists = etAssists.text.toString().toInt()
        } catch (nfe: NumberFormatException) {
            println("Could not parse $nfe")
            etKills.error = "cannot be blank"
            etDeaths.error = "cannot be blank"
            etAssists.error = "cannot be blank"
        }

        // Check if input fields are valid / display error
        if (date == null) {
            btnDate.error = "cannot be blank"
        }
        if (startTime == null) {
            btnStartTime.error = "cannot be blank"
        }
        if (endTime == null) {
            btnEndTime.error = "cannot be blank"
        }
        if (description!!.isEmpty()) {
            etDescription.error = "cannot be blank"
        }

        // Check if all info is valid
        if ((date != null && startTime != null && endTime != null) &&
            (kills >= 0 && deaths >= 0 && assists >= 0) &&
            (description!!.isNotEmpty() && category != "No Categories Found")) {
            // Calculate derived user game stats --> kill/death ratio and session playtime
            // Calculate K/D
            kd = if (kills > 0 && deaths > 0) {
                kills.toDouble() / deaths.toDouble()
            } else if (kills > 0 && deaths == 0) {
                kills.toDouble()
            } else if (kills == 0 && deaths == 0) {
                1.0
            } else {
                0.0
            }

            // Calculate Session Playtime
            val difference = startTime.time - endTime.time
            val days = (difference / (1000 * 60 * 60 * 24)).toDouble()
            val hours = ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60))
            // Get the playtime in hours
            playtime = if (hours < 0) -hours else hours

            if (filePath != null) {
                // Code for showing progressDialog while uploading
                val progressDialog = ProgressDialog(this@TimesheetActivity)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()

                // Defining the child of storageReference
                val ref = storageReference.child("Images/${UUID.randomUUID().toString()}")

                // Adding listeners on upload or failure of image
                val uploadTask = ref.putFile(filePath!!)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    filePathFS = taskSnapshot.metadata?.reference?.name

                    Log.d(TAG, "Adding Timesheet...")

                    // Initialize firebase objects
                    val db = FirebaseFirestore.getInstance()
                    val user = FirebaseAuth.getInstance().currentUser

                    // Setup the timesheet object
                    val entryObject: MutableMap<String, Any?> = HashMap()
                    entryObject["Category"] = category
                    entryObject["Description"] = description
                    entryObject["Date"] = dateFormat.format(date).toString()
                    entryObject["StartTime"] = timeFormat.format(startTime).toString()
                    entryObject["EndTime"] = timeFormat.format(endTime).toString()
                    entryObject["Kills"] = kills.toString()
                    entryObject["Deaths"] = deaths.toString()
                    entryObject["Assists"] = assists.toString()
                    entryObject["KD"] = String.format(Locale.getDefault(), "%.2f", kd)
                    entryObject["Playtime"] = String.format(Locale.getDefault(), "%.2f", playtime)
                    entryObject["ImagePath"] = filePathFS.toString()

                    // Initialize user object to be pushed to db
                    val userObject: MutableMap<String, Any?> = HashMap()

                    db.collection("Users/${user?.uid}/TimesheetEntries/$category/Timesheets")
                        .document()
                        .set(entryObject, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully written")
                        }
                        .addOnFailureListener { e ->
                            // Error, Image not uploaded
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@TimesheetActivity,
                                "Failed ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    progressDialog.dismiss()

                    // Clear values
                    btnDate.text = "Date"
                    btnStartTime.text = "Start Time"
                    btnEndTime.text = "End Time"
                    etDescription.setText("Description")
                    etKills.setText("Number of Kills")
                    etDeaths.setText("Number of Deaths")
                    etAssists.setText("Number of Assists")
                    Toast.makeText(
                        this@TimesheetActivity,
                        "Timesheet uploaded successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@TimesheetActivity, CategoryDetails::class.java)
                    intent.putExtra("object", category)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }.addOnFailureListener { e ->
                    // Error, Image not uploaded
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@TimesheetActivity,
                        "Failed ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnProgressListener { taskSnapshot ->
                    // Progress Listener for loading percentage on the dialog box
                    val progress =
                        100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog.setMessage("Uploaded ${progress.toInt()}%")
                }
            } else {
                btnUpload.error = "Please upload an image first"
                Toast.makeText(
                    this@TimesheetActivity,
                    "Please upload an image first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}

