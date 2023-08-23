package com.example.skilltrackv20_kotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CategoryDetailsAdapter(private val context: Context, private val timesheetArrayList: ArrayList<TimesheetEntry>) :
    RecyclerView.Adapter<CategoryDetailsAdapter.CategoryDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailsViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.cat_details_item, parent, false)
        return CategoryDetailsViewHolder(v)
    }

    override fun onBindViewHolder(holder: CategoryDetailsViewHolder, position: Int) {
        val timesheetEntry = timesheetArrayList[position]
        val db = FirebaseFirestore.getInstance()

        if (timesheetArrayList != null) {
            holder.tvCategoryName.text = "Date: ${timesheetEntry.Date}\n" +
                    "Description: ${timesheetEntry.Description}\n" +
                    "Start Time: ${timesheetEntry.StartTime} | End Time: ${timesheetEntry.EndTime}\n" +
                    "Playtime: ${timesheetEntry.Playtime} Hours\n" +
                    "K/D/A: ${timesheetEntry.Kills}/${timesheetEntry.Deaths}/${timesheetEntry.Assists}\n" +
                    "K/D: ${timesheetEntry.KD}"

            val ref: StorageReference =
                FirebaseStorage.getInstance().reference.child("Images/${timesheetEntry.ImagePath}")
            Glide.with(context).load(ref).into(holder.imgView)
        } else if (timesheetEntry == null) {
            holder.tvCategoryName.text = "No Timesheets Added"
            Glide.with(context).load(R.drawable.nocat).into(holder.imgView)
        }
    }

    override fun getItemCount(): Int {
        return timesheetArrayList.size
    }

    inner class CategoryDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        var imgView: ImageView = itemView.findViewById(R.id.imgView)
        var rootView: View = itemView
        var btnViewCategory: RelativeLayout = itemView.findViewById(R.id.btnViewCategory)
        var timesheetArrayList: ArrayList<TimesheetEntry> = ArrayList()
    }
}
