package com.jery.feedformulation6.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.jery.feedformulation6.utils.Utils
import java.io.File
import java.io.FileOutputStream

data class Feed(
    val name: String,
    var cost: Float,
    val type: String,
    val details: List<Float>,
    val percentage: Map<String, Float>,
    var checked: Boolean,
    val id: Int
) {
    companion object {
        fun addNewFeed(feed: Feed, feedsFile: File) {
            val feedsJson = Utils.loadJSONFromFile(feedsFile.path)
            val existingArray = JsonParser.parseString(feedsJson).asJsonArray
            val feedJson = GsonBuilder().setPrettyPrinting().create().toJsonTree(feed).asJsonObject
            existingArray.add(feedJson)
            val updatedFeedsJson = GsonBuilder().setPrettyPrinting().create().toJson(existingArray)
            FileOutputStream(feedsFile).use { outputStream ->
                outputStream.write(updatedFeedsJson.toByteArray())
            }
        }
    }

    fun getCategoryText(): String {
        return try {
            when (type) {
                "R" -> "Roughages"
                "C" -> "Concentrates"
                "M" -> "Minerals"
                else -> "Other"
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            ""
        }
    }
}
