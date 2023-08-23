package com.example.skilltrackv20_kotlin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CategoryDetails : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var imgBackground: ImageView
    private lateinit var imgView2: ImageView
    private lateinit var rlNoData: RelativeLayout
    private lateinit var cvNoData: CardView
    private lateinit var emptyText: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var categoryDetailsAdapter: CategoryDetailsAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var progressDialog: ProgressDialog
    private var entryArrayList: ArrayList<TimesheetEntry> = ArrayList()
    private var categoryArrayList: ArrayList<Category> = ArrayList()

    //Date Filter
    private val myCalendar = Calendar.getInstance()
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnFilter: Button

    //Timeframe filter - Bonus Feature
    var fabFilterCycle: Int = 0
    private lateinit var fabOneMonthFilter: FloatingActionButton
    private lateinit var fabOneWeekFilter: FloatingActionButton
    private lateinit var fabReset: FloatingActionButton

    //Game Stats Screen - Bonus Feature
    private lateinit var fabGameStats: FloatingActionButton

    //Graph
    lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        val categoryName = intent.getStringExtra("object")
        var fabFilterCycle: Int = 0

        //Type cast the image view
        imgBackground = findViewById(R.id.imgBackground)
        imgView2 = findViewById(R.id.imgView2)
        //use glide to animate image with gif
        Glide.with(this).asGif().load(R.drawable.bg5).into(imgBackground)
        Glide.with(this).asGif().load(R.drawable.nocat).into(imgView2)

        //Typecast the Pie chart
        pieChart = findViewById(R.id.pie_chart)

        //Typecast date filter buttons
        btnStartDate = findViewById(R.id.startDateButton)
        btnEndDate = findViewById(R.id.endDateButton)
        btnFilter = findViewById(R.id.filterButton)

        //Typecast the Data Views
        rlNoData = findViewById(R.id.rlNoData)
        cvNoData = findViewById(R.id.cvNoData)
        emptyText = findViewById(R.id.tv_no_data)

        //Setup Firebase and recycler view adapter
        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser!!
        categoryDetailsAdapter = CategoryDetailsAdapter(this, entryArrayList)

        //Typecast and Init the recycler view
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = categoryDetailsAdapter

        //Floating action buttons
        fab = findViewById(R.id.fabTimesheet)
        fab.setOnClickListener {
            startActivity(Intent(applicationContext, TimesheetActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }//End of Fab

        //One Week Progress Filter
        fabOneWeekFilter = findViewById(R.id.fabOneWeek)
        fabOneWeekFilter.setOnClickListener {
            //Get the past weeks data
            val thisCalendar = Calendar.getInstance()
            val originalDate: Date = thisCalendar.getTime()
            thisCalendar.add(Calendar.WEEK_OF_MONTH, -1)
            val pastDate: Date = thisCalendar.getTime()
            val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            var pastDateOutput = format.format(pastDate)
            var originalDateOutput = format.format(originalDate)
            //Filter the chart with the data
            FilterChart(pastDateOutput, originalDateOutput)
        }//End of Fab

        //One Month Progress Filter
        fabOneMonthFilter = findViewById(R.id.fabOneMonth)
        fabOneMonthFilter.setOnClickListener {
            //Get the past months dates
            val thisCalendar = Calendar.getInstance()
            val originalDate: Date = thisCalendar.getTime()
            thisCalendar.add(Calendar.MONTH, -1)
            val pastDate: Date = thisCalendar.getTime()
            val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            var pastDateOutput = format.format(pastDate)
            var originalDateOutput = format.format(originalDate)
            //Filter the chart with the data
            FilterChart(pastDateOutput, originalDateOutput)
        }//End of Fab

        //Reset Chart
        fabReset = findViewById(R.id.fabReset)
        fabReset.setOnClickListener {
            val intent = intent
            finish()
            startActivity(intent)
        }//End of Fab

        //GameStats Fab
        fabGameStats = findViewById(R.id.fabGameStats)
        fabGameStats.setOnClickListener {
            val categoryName = intent.getStringExtra("object")
            val intent = Intent(this, GameStatsActivity::class.java)
            intent.putExtra("object", categoryName)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }//End of Fab

        //Add progress dialog for load
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading...")
        progressDialog.show()

        //Load Timesheet Data from Firestore
        eventChangeListener()

        //Setup bottom NavBar
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

        // Button On Clicks
        // Start Date Picker
        val startDate =
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, day: Int ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = month
                myCalendar[Calendar.DAY_OF_MONTH] = day
                updateStartDateLabel()
            }
        btnStartDate.setOnClickListener {
            DatePickerDialog(
                this,
                startDate,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        // End Date Picker
        val endDate =
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, day: Int ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = month
                myCalendar[Calendar.DAY_OF_MONTH] = day
                updateEndDateLabel()
            }
        btnEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                endDate,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        //Filter Button
        btnFilter.setOnClickListener {
           FilterChart(btnStartDate.text.toString(), btnEndDate.text.toString())
        }
    }

    private fun updateStartDateLabel() {
        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        btnStartDate.text = sdf.format(myCalendar.time)
    }

    private fun updateEndDateLabel() {
        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        btnEndDate.text = sdf.format(myCalendar.time)
    }

    private fun eventChangeListener() {
        val categoryName = intent.getStringExtra("object")

        db.collection("Users/${user.uid}/TimesheetEntries/$categoryName/Timesheets")
            .whereEqualTo("Category", categoryName)
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        for (document in task.result!!) {
                            if (document != null) {
                                entryArrayList.add(document.toObject(TimesheetEntry::class.java))
                                cvNoData.visibility = View.INVISIBLE

                            }
                        }
                        if (entryArrayList.size >= 1) {
                            //Populate Chart
                            PopulateChart(entryArrayList)
                        }
                        if (entryArrayList.size < 1) {
                            entryArrayList.clear()
                            categoryDetailsAdapter.notifyDataSetChanged()
                            cvNoData.visibility = View.VISIBLE
                            if (progressDialog.isShowing) {
                                progressDialog.dismiss()
                            }
                        }
                    }
                    if (progressDialog.isShowing) {
                        categoryDetailsAdapter.notifyDataSetChanged()
                        progressDialog.dismiss()
                    }
                } else {
                    if (progressDialog.isShowing) {
                        progressDialog.setCancelable(true)
                        progressDialog.setMessage("Error loading data \n Please check your internet connection")
                        Log.v("testvalue", "Error getting documents: ", task.exception)
                        return@addOnCompleteListener
                    }
                }
            }
    }

    private fun PopulateChart(entryArrayList: ArrayList<TimesheetEntry>) {

        //Setup variables to hold calc data
        var list: ArrayList<PieEntry> = ArrayList()
        var minHours: Int = 0
        var maxHours: Int = 0
        var totalHours: Float = 0.0f
        var requiredHours: Float = 0.0f
        var requiredMaxHours: Float = 0.0f

        //Get category Min hour data
        db.collection("Users/${user.uid}/Categories").get()
            .addOnCompleteListener(OnCompleteListener { task ->
                when{
                    task.isSuccessful -> {
                        val documents = task.result
                        for (document: QueryDocumentSnapshot in documents)
                        {
                            categoryArrayList.add(document.toObject(Category::class.java))
                            minHours = GetCategoryMin(categoryArrayList)
                            maxHours = GetCategoryMax(categoryArrayList)
                            Log.v("testvalue", "Min Hours before: $minHours \n", null)
                        }
                        //Find the amount of days the user has being playing this game for
                        entryArrayList.sortBy { Date.parse(it.Date) }
                        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                        val startDate = dateFormat.parse(entryArrayList[0].Date)
                        val endDate = dateFormat.parse(entryArrayList.last().Date)
                        val difference = kotlin.math.abs(startDate.time - Date().time)
                        val days = difference / (24 * 60 * 60 * 1000)

                        //calc the required days
                        requiredHours = (days * minHours).toFloat()
                        requiredMaxHours = (days * maxHours).toFloat()
                        Log.v("testvalue", "Required Hours: $requiredHours \n Min Hours: $minHours", null)

                        //If timesheet data exists, calculate daily goals performance
                        for (entries in entryArrayList.iterator()) {
                            //Convert playtime from Firebase to a Float and add it to the counter
                            totalHours += (entries.Playtime!!.replace(",", ".")).toFloat()

                            //logcat debugging V Logs
                            if (entries.Playtime != null) {
                                Log.v("testvalue", "Playtime" + entries.Playtime + "\n", null)
                            } else {
                                Log.v("testvalue", "Playtime: NADA", null)
                            }
                        }

                        //Setup chart data
                        list.add(PieEntry(totalHours, "Total Playtime"))
                        list.add(PieEntry(requiredHours, "Min Required Playtime"))
                        list.add(PieEntry(requiredMaxHours, "Max Required Playtime"))

                        //Set Goal Progress indicator
                        if (totalHours > requiredHours)
                        {
                            pieChart.setCenterTextColor(Color.GREEN)
                            pieChart.centerText = "Complete"
                        }

                        if (totalHours < requiredHours)
                        {
                            pieChart.setCenterTextColor(Color.YELLOW)
                            pieChart.centerText = "In Progress"
                        }

                        if (totalHours > requiredMaxHours)
                        {
                            pieChart.setCenterTextColor(Color.MAGENTA)
                            pieChart.centerText = "Perfection"
                        }

                        //Populated the chart
                        val pieDataSet = PieDataSet(list, "List")
                        val pieData = PieData(pieDataSet)
                        pieChart.data = pieData

                        //Chart Decorations
                        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS, 172)
                        pieDataSet.valueTextColor = Color.WHITE
                        pieDataSet.valueTextSize = 15f
                        pieChart.legend.textColor = Color.WHITE
                        pieChart.description.text = "Category Daily Performance"
                        pieChart.setEntryLabelColor(Color.WHITE)
                        pieChart.setHoleColor(Color.BLACK)

                        //default center text
                        pieChart.setCenterTextColor(Color.WHITE)
                        pieChart.centerText = "Progress"

                        //Set Goal Progress indicator
                        if (totalHours > requiredHours || totalHours == requiredHours)
                        {
                            pieChart.setCenterTextColor(Color.GREEN)
                            pieChart.centerText = "Complete"
                        }
                        else if (totalHours < requiredHours)
                        {
                            pieChart.setCenterTextColor(Color.YELLOW)
                            pieChart.centerText = "In Progress"
                        }

                        if (totalHours >= requiredMaxHours)
                        {
                            pieChart.setCenterTextColor(Color.MAGENTA)
                            pieChart.centerText = "Perfection"
                        }

                        //Animate pie chart
                        pieChart.animateY(350)

                    }
                    else -> {
                        Log.v("testvalue", "Error Loading Category \n", null)
                    }
                }
            })//end listener
    }//End populate chart method

    //Method to get category min hours
    private fun GetCategoryMin(categoryArrayList: ArrayList<Category> = ArrayList()): Int {

        val categoryName = intent.getStringExtra("object")
        //init category Min hours
        categoryArrayList.forEach {
            if (it.CategoryName == categoryName) {
                val categoryName = intent.getStringExtra("object")
                return it.CategoryMin!!.toInt()
                Log.v("testvalue", "Category: ${it.CategoryName} \nminHours: ${it.CategoryMin!!.toInt()}", null)
            }
        }
        //If non found
        Log.v("testvalue", "Category: None \nminHours: None", null)
        return 0
    }//end method

    //Method to get category min hours
    private fun GetCategoryMax(categoryArrayList: ArrayList<Category> = ArrayList()): Int {

        val categoryName = intent.getStringExtra("object")
        //init category Min hours
        categoryArrayList.forEach {
            if (it.CategoryName == categoryName) {
                val categoryName = intent.getStringExtra("object")
                return it.CategoryMax!!.toInt()
                Log.v("testvalue", "Category: ${it.CategoryName} \nmaxHours: ${it.CategoryMax!!.toInt()}", null)
            }
        }
        //If non found
        Log.v("testvalue", "Category: None \nmaxHours: None", null)
        return 0
    }//end method

    private fun FilterChart(StartDate: String, EndDate: String) {
        //Setup variables to hold calc data
        var list: ArrayList<PieEntry> = ArrayList()
        var minHours: Int = 0
        var maxHours: Int = 0
        var totalHours: Float = 0.0f
        var requiredHours: Float = 0.0f
        var requiredMaxHours: Float = 0.0f
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        var cvData = cvNoData.visibility.toString()
        var invisible = View.INVISIBLE.toString()
        categoryArrayList.clear()

        //Check if the user entered a date range, and that timesheet data exists
        if (StartDate != "Start Date" && EndDate != "End Date" && (cvData == invisible)) {

            //Get Filter Date Range
            val startDate: Date = dateFormat.parse(StartDate)
            val endDate: Date = dateFormat.parse(EndDate)

            if (endDate > startDate) {
                //Get category Min hour data
                db.collection("Users/${user.uid}/Categories").get()
                    .addOnCompleteListener(OnCompleteListener { task ->
                        when {
                            task.isSuccessful -> {
                                val documents = task.result
                                for (document: QueryDocumentSnapshot in documents) {
                                    categoryArrayList.add(document.toObject(Category::class.java))
                                    minHours = GetCategoryMin(categoryArrayList)
                                    maxHours = GetCategoryMax(categoryArrayList)
                                    Log.v("testvalue", "Min Hours before: $minHours \n", null)
                                }
                                val difference = kotlin.math.abs(startDate.time - endDate.time)
                                val days = difference / (24 * 60 * 60 * 1000)

                                //calc the required days
                                requiredHours = (days * minHours).toFloat()
                                requiredMaxHours = (days * maxHours).toFloat()
                                Log.v(
                                    "testvalue",
                                    "Required Hours: $requiredHours \n Min Hours: $minHours \n Max Hours: $maxHours",
                                    null
                                )

                                //If timesheet data exists, calculate daily goals performance
                                for (entries in entryArrayList.iterator()) {

                                    //if the entries date is <= filter end date, add it to the total time
                                    if (dateFormat.parse(entries.Date) <= endDate && dateFormat.parse(entries.Date) >= startDate) {
                                        //Convert playtime from Firebase to a Float and add it to the counter
                                        totalHours += (entries.Playtime!!.replace(
                                            ",",
                                            "."
                                        )).toFloat()
                                    }
                                    //logcat debugging V Logs
                                    if (entries.Playtime != null) {
                                        Log.v(
                                            "testvalue",
                                            "Playtime" + entries.Playtime + "\n",
                                            null
                                        )
                                    } else {
                                        Log.v("testvalue", "Playtime: NADA", null)
                                    }
                                }

                                //Setup chart data
                                list.add(PieEntry(totalHours, "Total Playtime"))
                                list.add(PieEntry(requiredHours, "Min Required Playtime"))
                                list.add(PieEntry(requiredMaxHours, "Max Required Playtime"))

                                //Populated the chart
                                val pieDataSet = PieDataSet(list, "List")
                                val pieData = PieData(pieDataSet)
                                pieChart.data = pieData

                                //Chart Decorations
                                pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS, 172)
                                pieDataSet.valueTextColor = Color.WHITE
                                pieDataSet.valueTextSize = 15f
                                pieChart.legend.textColor = Color.WHITE
                                pieChart.description.text = "Category Daily Performance"
                                pieChart.setEntryLabelColor(Color.WHITE)
                                pieChart.setHoleColor(Color.BLACK)

                                //default center text
                                pieChart.setCenterTextColor(Color.WHITE)
                                pieChart.centerText = "Progress"

                                //Set Goal Progress indicator
                                if (totalHours > requiredHours || totalHours == requiredHours)
                                {
                                    pieChart.setCenterTextColor(Color.GREEN)
                                    pieChart.centerText = "Complete"
                                }
                                else if (totalHours < requiredHours)
                                {
                                    pieChart.setCenterTextColor(Color.YELLOW)
                                    pieChart.centerText = "In Progress"
                                }

                                if (totalHours >= requiredMaxHours)
                                {
                                    pieChart.setCenterTextColor(Color.MAGENTA)
                                    pieChart.centerText = "Perfection"
                                }

                                //Animate pie chart
                                pieChart.animateY(350)

                            }

                            else -> {
                                Log.v("testvalue", "Error Loading Category \n", null)
                            }
                        }
                    })//end listener
            }//end if
            else
            {
                btnStartDate.error = "cannot be later than End Date"
                btnEndDate.error = "cannot be earlier than Start Date"
            }
        }//end date range check
        else
        {
            btnStartDate.error = "cannot be blank"
            btnEndDate.error = "cannot be blank"
        }
    }//end method

}
