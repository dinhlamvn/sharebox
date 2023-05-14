package com.dinhlam.sharebox.data.local.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.dinhlam.sharebox.extensions.enumByNameIgnoreCase
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareType
import com.google.gson.Gson
import com.google.gson.JsonObject

@ProvidedTypeConverter
class ShareDataConverter constructor(
    private val gson: Gson
) {
    @TypeConverter
    fun shareDataToString(shareData: ShareData): String {
        return when (shareData) {
            is ShareData.ShareUrl -> gson.toJson(shareData, ShareData.ShareUrl::class.java)
            is ShareData.ShareText -> gson.toJson(shareData, ShareData.ShareText::class.java)
            is ShareData.ShareImage -> gson.toJson(shareData, ShareData.ShareImage::class.java)
            is ShareData.ShareImages -> gson.toJson(shareData, ShareData.ShareImages::class.java)
        }
    }

    @TypeConverter
    fun stringToShareData(str: String): ShareData {
        val json = gson.fromJson(str, JsonObject::class.java)
        return when (enumByNameIgnoreCase(json.get("type").asString, ShareType.UNKNOWN)) {
            ShareType.URL -> gson.fromJson(json, ShareData.ShareUrl::class.java)
            ShareType.TEXT -> gson.fromJson(json, ShareData.ShareText::class.java)
            ShareType.IMAGE -> gson.fromJson(json, ShareData.ShareImage::class.java)
            ShareType.IMAGES -> gson.fromJson(json, ShareData.ShareImages::class.java)
            else -> error("Error while parse json string $str to ShareData")
        }
    }
}