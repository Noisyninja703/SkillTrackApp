package com.example.skilltrackv20_kotlin

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.gms.tasks.OnSuccessListener
import com.google.common.primitives.Bytes
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList

class CategoryAdapter(
    private val context: Context,
    private val categoryArrayList: ArrayList<Category>,
    private val mListener: ICategoryDetails
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    interface ICategoryDetails {
        fun GotoCategoryDetails(category: Category)
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val imgView: ImageView = itemView.findViewById(R.id.imgView)
        val btnViewCategory: RelativeLayout = itemView.findViewById(R.id.btnViewCategory)
        var category: Category? = null

        init {
            btnViewCategory.setOnClickListener {
                Log.d("demo", "Onclick item clicked $adapterPosition Category: ${category?.CategoryName}")
                mListener.GotoCategoryDetails(category!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return CategoryViewHolder(v)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryArrayList[position]

        if (category != null) {
            holder.tvCategoryName.text =
                "${category.CategoryName}\nMin Daily: ${category.CategoryMin} Hours\nMax Daily: ${category.CategoryMax} Hours"
            holder.category = category

            val ref: StorageReference = FirebaseStorage.getInstance()
                .reference.child("Images/${category.ImagePath}")
            Glide.with(context).load(ref).into(holder.imgView)
        } else {
            holder.tvCategoryName.text = "No Categories Added"
            Glide.with(context).load(R.drawable.nocat).into(holder.imgView)
        }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }
}
