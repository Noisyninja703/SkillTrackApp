package com.example.skilltrackv20_kotlin

class TimesheetEntry {
    // Basic entry requirements
    var Date: String? = null
    var StartTime: String? = null
    var EndTime: String? = null
    var Description: String? = null
    var Category: String? = null
    var ImagePath: String? = null

    // Game related entry stats
    var Kills: String? = null
    var Deaths: String? = null
    var Assists: String? = null
    var KD: String? = null
    var Playtime: String? = null

    // Constructor for RecyclerView Adapter level access
    constructor()

    // Constructor for app level access
    constructor(
        date: String?,
        startTime: String?,
        endTime: String?,
        description: String?,
        category: String?,
        imagePath: String?,
        kills: String?,
        deaths: String?,
        assists: String?,
        kd: String?,
        playtime: String?
    ) {
        this.Date = date
        this.StartTime = startTime
        this.EndTime = endTime
        this.Description = description
        this.Category = category
        this.ImagePath = imagePath
        this.Kills = kills
        this.Deaths = deaths
        this.Assists = assists
        this.KD = kd
        this.Playtime = playtime
    }

    // Getters and Setters

    // Basic requirements

    @JvmName("funGetDate")
    fun getDate(): String? {
        return Date
    }

    @JvmName("funSetDate")
    fun setDate(date: String?) {
        this.Date = date
    }

    @JvmName("funGetStartTime")
    fun getStartTime(): String? {
        return StartTime
    }

    @JvmName("funSetStartTime")
    fun setStartTime(startTime: String?) {
        this.StartTime = startTime
    }

    @JvmName("funGetEndTime")
    fun getEndTime(): String? {
        return EndTime
    }

    @JvmName("funSetEndTime")
    fun setEndTime(endTime: String?) {
        this.EndTime = endTime
    }

    @JvmName("funGetDescription")
    fun getDescription(): String? {
        return Description
    }

    @JvmName("funSetDescription")
    fun setDescription(description: String?) {
        this.Description = description
    }

    @JvmName("funGetCategory")
    fun getCategory(): String? {
        return Category
    }

    @JvmName("funSetCategory")
    fun setCategory(category: String?) {
        this.Category = category
    }

    @JvmName("funGetImagePath")
    fun getImagePath(): String? {
        return ImagePath
    }

    @JvmName("funSetImagePath")
    fun setImagePath(imagePath: String?) {
        this.ImagePath = imagePath
    }

    // Game stat requirements

    @JvmName("funGetKills")
    fun getKills(): String? {
        return Kills
    }

    @JvmName("funSetKills")
    fun setKills(kills: String?) {
        this.Kills = kills
    }

    @JvmName("funGetDeaths")
    fun getDeaths(): String? {
        return Deaths
    }

    @JvmName("funSetDeaths")
    fun setDeaths(deaths: String?) {
        this.Deaths = deaths
    }

    @JvmName("funGetAssists")
    fun getAssists(): String? {
        return Assists
    }

    @JvmName("funSetAssists")
    fun setAssists(assists: String?) {
        this.Assists = assists
    }

    @JvmName("funGetKD")
    fun getKd(): String? {
        return KD
    }

    @JvmName("funSetKD")
    fun setKd(kd: String?) {
        this.KD = kd
    }

    @JvmName("funGetPlaytime")
    fun getPlaytime(): String? {
        return Playtime
    }

    @JvmName("funSetPlaytime")
    fun setPlaytime(playtime: String?) {
        this.Playtime = playtime
    }
}
