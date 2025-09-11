/*
 * Room type converters for complex data types
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String): Map<String, String>? {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }
    
    @TypeConverter
    fun fromDoubleMap(value: Map<String, Double>?): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toDoubleMap(value: String): Map<String, Double>? {
        val mapType = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(value, mapType)
    }
}
