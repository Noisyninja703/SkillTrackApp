package com.example.skilltrackv20_kotlin
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.compose.foundation.interaction.DragInteraction
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
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
import kotlin.math.abs

class GameStatsActivity : AppCompatActivity() {

    lateinit var barChart:BarChart
    private lateinit var db: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var progressDialog: ProgressDialog
    private var entryArrayList: ArrayList<TimesheetEntry> = ArrayList()

    //KDA Bar - bonus feature
    // Variables
    private lateinit var imgBackground: ImageView
    private lateinit var ivKD: ImageView
    private lateinit var ivKills: ImageView
    private lateinit var ivDeaths: ImageView
    private lateinit var ivAssists: ImageView
    private lateinit var cv1: RelativeLayout
    private lateinit var cv2: RelativeLayout
    private lateinit var cv3: RelativeLayout
    private lateinit var cv4: RelativeLayout
    private lateinit var tvKD: TextView
    private lateinit var tvKills: TextView
    private lateinit var tvDeaths: TextView
    private lateinit var tvAssists: TextView

    //Timeframe filter - Bonus Feature
    var fabFilterCycle: Int = 0
    private lateinit var fabOneMonthFilter: FloatingActionButton
    private lateinit var fabOneWeekFilter: FloatingActionButton
    private lateinit var fabReset: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_stats)

        //Typecast barchart
        barChart=findViewById(R.id.bar_chart)

        // Type cast card views
        cv1 = findViewById(R.id.rlKD)
        cv2 = findViewById(R.id.rlKills)
        cv3 = findViewById(R.id.rlDeaths)
        cv4 = findViewById(R.id.rlAssists)

        //Type cast text views
        tvKD = findViewById(R.id.tv_KD)
        tvKills = findViewById(R.id.tv_Kills)
        tvDeaths = findViewById(R.id.tv_Deaths)
        tvAssists = findViewById(R.id.tv_Assists)

        // Type cast the image view
        imgBackground = findViewById(R.id.imgBackground)
        ivKD = findViewById(R.id.ivKD)
        ivKills = findViewById(R.id.ivKills)
        ivDeaths = findViewById(R.id.ivDeaths)
        ivAssists = findViewById(R.id.ivAssists)

        // Use Glide to animate image with gif
        Glide.with(this).asGif().load(R.drawable.loginani).into(imgBackground)
        Glide.with(this).asGif().load(R.drawable.ghostkd).into(ivKD)
        Glide.with(this).asGif().load(R.drawable.ghostkill).into(ivKills)
        Glide.with(this).asGif().load(R.drawable.ghostdeath).into(ivDeaths)
        Glide.with(this).asGif().load(R.drawable.ghostassist).into(ivAssists)

        //Firebase setup
        db = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser!!

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

        eventChangeListener(null)

        //Fabs
        //One Week Progress Filter
        fabOneWeekFilter = findViewById(R.id.fabOneWeek)
        fabOneWeekFilter.setOnClickListener {
            //Get the past weeks data
            val thisCalendar = Calendar.getInstance()
            thisCalendar.add(Calendar.WEEK_OF_MONTH, -1)
            val pastDate: Date = thisCalendar.getTime()
            //Filter the chart with the data
            eventChangeListener(pastDate)
        }//End of Fab

        //One Month Progress Filter
        fabOneMonthFilter = findViewById(R.id.fabOneMonth)
        fabOneMonthFilter.setOnClickListener {
            //Get the past months dates
            val thisCalendar = Calendar.getInstance()
            thisCalendar.add(Calendar.MONTH, -1)
            val pastDate: Date = thisCalendar.getTime()
            //Filter the chart with the data
            eventChangeListener(pastDate)
        }//End of Fab

        //Reset Chart
        fabReset = findViewById(R.id.fabReset)
        fabReset.setOnClickListener {
            val intent = intent
            finish()
            startActivity(intent)
        }//End of Fab

    }// On Create ends

    //Populate the chart
    private fun eventChangeListener(startDate: Date?) {
        val categoryName = intent.getStringExtra("object")
        entryArrayList.clear()

        //Get all timesheet entries for this game
        db.collection("Users/${user.uid}/TimesheetEntries/$categoryName/Timesheets")
            .whereEqualTo("Category", categoryName)
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                if (task.isSuccessful) {
                    if (task.result != null) {
                        for (document in task.result!!) {
                            if (document != null) {
                                entryArrayList.add(document.toObject(TimesheetEntry::class.java))
                            }
                        }
                        //if there is data, populate chart
                        if (entryArrayList.size >= 1) {
                            //Populate Chart
                            if (startDate != null)
                            {
                                PopulateChart(entryArrayList, startDate) //If method is overloaded with startDate
                            }
                            else
                            {
                                PopulateChart(entryArrayList, null)
                            }
                        }
                    }
                }
            }//end on change listener
    } //event listener changed end

    private fun PopulateChart(entryArrayList: ArrayList<TimesheetEntry>, StartDate: Date?) {

        //Setup variables to hold calc data
        var list: ArrayList<PieEntry> = ArrayList()
        var minHours: Int = 0
        var maxHours: Int = 0
        var totalHours: Float = 0.0f
        var kd: Float = 0.0f
        var avgKD: Float = 0.0f
        val labels = ArrayList<String>()
        val datas = ArrayList<BarEntry>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        //Lists to hold stat calc data
        val avgKDList = ArrayList<Double>()
        val avgKills = ArrayList<Double>()
        val avgDeaths = ArrayList<Double>()
        val avgAssists = ArrayList<Double>()
        var avgArray = ArrayList<Double>()

        //if not overloaded with date filters, give default load
        if (StartDate == null) {
            //Sort the list so the chart data is populated in Descending order by date
            entryArrayList.sortByDescending { Date.parse(it.Date) }
            //setup chart XAxis Iterator
            var x: Int = 0
            //If timesheet data exists, calculate daily goals performance
            for (entries in entryArrayList.iterator()) {
                //add data and plot x and y coordinates for bar entries
                kd = (entries.KD!!.replace(",", ".").toFloat())
                datas.add(BarEntry(x.toFloat(), kd))
                x++
                labels.add(dateFormat.format((Date.parse(entries.Date))))

                //add data to stat calc lists
                avgKDList.add(entries.KD!!.replace(",", ".").toDouble())
                avgKills.add(entries.Kills!!.replace(",", ".").toDouble())
                avgDeaths.add(entries.Deaths!!.replace(",", ".").toDouble())
                avgAssists.add(entries.Assists!!.replace(",", ".").toDouble())
            }//end for

            //calc the averages
            Log.v("Begin", "Begin Stat Calc", null)
            avgArray = CalcStats(avgKDList, avgKills, avgDeaths, avgAssists)
            RefreshChart(labels, datas, avgArray)//update the chart
        }//end default load if

        //if overloaded with date filters, give filtered load
        if (StartDate != null) {
            //Find the amount of days the user has being playing this game for
            entryArrayList.sortBy { Date.parse(it.Date) }
            //setup chart XAxis Iterator
            var x: Int = 0

            //If timesheet data exists, calculate daily goals performance
            for (entries in entryArrayList.iterator()) {
                //add data and plot x and y coordinates for bar entries
                //Filter by Date
                if (Date.parse(entries.Date) >= StartDate.time) {
                    kd = (entries.KD!!.replace(",", ".").toFloat())
                    datas.add(BarEntry(x.toFloat(), kd))
                    x++
                    labels.add(dateFormat.format((Date.parse(entries.Date))))
                    //add data to stat calc lists
                    avgKDList.add(entries.KD!!.replace(",", ".").toDouble())
                    avgKills.add(entries.Kills!!.replace(",", ".").toDouble())
                    avgDeaths.add(entries.Deaths!!.replace(",", ".").toDouble())
                    avgAssists.add(entries.Assists!!.replace(",", ".").toDouble())
                }//end date check
            }//end for

            //calc the averages
            avgArray = CalcStats(avgKDList, avgKills, avgDeaths, avgAssists)
            RefreshChart(labels, datas, avgArray)//update the chart
        }//end filtered load if
    }//End populate chart method

    private fun CalcStats(avgKD: ArrayList<Double>, avgKills: ArrayList<Double>, avgDeaths: ArrayList<Double>, avgAssists: ArrayList<Double>) : ArrayList<Double>
    {
        var kdCount: Int = 0
        var killCount: Int = 0
        var deathCount: Int = 0
        var assistCount: Int = 0
        var kd: Double = 0.0
        var kills: Double = 0.0
        var deaths: Double = 0.0
        var assists: Double = 0.0

        //get the total number of days and the total kd gained on those days
        for (entries in avgKD)
        {
            kdCount++
            kd += entries
        }
        //Get the avg KD
        val averageKD: Double = kd/kdCount
        Log.v("CalcStats", "KD: $averageKD", null)

        //get the total number of days and the total kills gained on those days
        for (entries in avgKills)
        {
            killCount++
            kills += entries
        }
        //Get the avg KD
        val averageKills: Double = kills/killCount
        Log.v("CalcStats", "Kills: $averageKills", null)

        //get the total number of days and the total deaths gained on those days
        for (entries in avgDeaths)
        {
            deathCount++
            deaths += entries
        }
        //Get the avg KD
        val averageDeaths: Double = deaths/deathCount
        Log.v("CalcStats", "Deaths: $averageDeaths", null)

        //get the total number of days and the total assists gained on those days
        for (entries in avgAssists)
        {
            assistCount++
            assists += entries
        }
        //Get the avg KD
        val averageAssists: Double = assists/assists
        Log.v("CalcStats", "Assists: $averageAssists", null)

        val avgArray = ArrayList<Double>()
        avgArray.add(averageKD)
        avgArray.add(averageKills)
        avgArray.add(averageDeaths)
        avgArray.add(averageAssists)
        return avgArray
    }

    private fun RefreshChart(labels: ArrayList<String>, datas: ArrayList<BarEntry>, avgArray: ArrayList<Double>)
    {
        Log.v("testvalue", "Data: $datas\n", null)
        Log.v("testvalue", "Labels: $labels\n", null)
        Log.v("testvalue", "Out of Loop", null)

        //Refresh averages
        tvKD.setText("Avg KD: ${String.format(Locale.getDefault(), "%.2f", avgArray[0])}")
        tvKills.setText("Avg K: ${String.format(Locale.getDefault(), "%.2f", avgArray[1])}")
        tvDeaths.setText("Avg D: ${String.format(Locale.getDefault(), "%.2f", avgArray[2])}")
        tvAssists.setText("Avg A: ${String.format(Locale.getDefault(), "%.2f", avgArray[3])}")

        //Setup Barchart
        val barDataSet= BarDataSet(datas,"List")
        val xAxis = barChart.getXAxis()
        val yAxis = barChart.getAxis(YAxis.AxisDependency.LEFT)
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        yAxis.textColor = Color.WHITE
        xAxis.textColor = Color.WHITE
        xAxis.labelCount = labels.size
        xAxis.setSpaceMax(0.5f)
        xAxis.setSpaceMin(0.5f)
        xAxis.valueFormatter = object : ValueFormatter() {
            override
            fun getFormattedValue(value: Float): String {
                // value is x as index
                return labels[value.toInt()]
            }
        }
        barDataSet.setColors(ColorTemplate.JOYFUL_COLORS,172)
        barDataSet.valueTextColor=Color.WHITE
        barDataSet.valueTextSize=12f

        //Populate barchart
        val barData= BarData(barDataSet)
        barChart.setFitBars(true)
        barChart.data= barData
        barChart.description.text= "Bar Chart"
        barChart.animateY(350)
    }
}