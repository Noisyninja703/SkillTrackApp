package com.example.skilltrackv20_kotlin

class Category {
    // Basic category requirements
     var CategoryName: String? = null
     var ImagePath: String? = null
     var CategoryMin: String? = null
     var CategoryMax: String? = null

    // Empty constructor for RecyclerView Adapter db access
    constructor()

    // Constructor for app level access
    constructor(categoryName: String?, imagePath: String?, categoryMax: String?, categoryMin: String?) {
        this.CategoryName = categoryName
        this.ImagePath = imagePath
        this.CategoryMin = categoryMin
        this.CategoryMax = categoryMax
    }

    // Getters and Setters
    @JvmName("funGetCategoryName")
    fun getCategoryName(): String? {
        return CategoryName
    }

    @JvmName("funSetCategoryName")
    fun setCategoryName(categoryName: String?) {
        this.CategoryName = categoryName
    }

    @JvmName("funGetCategoryImagePath")
    fun getImagePath(): String? {
        return ImagePath
    }

    @JvmName("funSetCategoryImagePath")
    fun setImagePath(imagePath: String?) {
        this.ImagePath = imagePath
    }

    @JvmName("funGetCategoryMin")
    fun getCategoryMin(): String? {
        return CategoryMin
    }

    @JvmName("funSetCategoryMin")
    fun setCategoryMin(categoryMin: String?) {
        this.CategoryMin = categoryMin
    }

    @JvmName("funGetCategoryMax")
    fun getCategoryMax(): String? {
        return CategoryMax
    }

    @JvmName("funSetCategoryMax")
    fun setCategoryMax(categoryMax: String?) {
        this.CategoryMax = categoryMax
    }
}
