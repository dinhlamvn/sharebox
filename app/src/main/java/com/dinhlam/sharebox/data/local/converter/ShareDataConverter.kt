package com.dinhlam.sharebox.data.local.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.dinhlam.sharebox.extensions.enumByNameIgnoreCase
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareType
import com.google.gson.Gson
import com.google.gson.JsonObject

@ProvidedTypeConverter
class ShareDataConverter constructor(
    private val gson: Gson
) {
    @TypeConverter
    fun shareDataToString(shareData: ShareData): String {
        val clazz = when (shareData) {
            is ShareData.ShareUrl -> ShareData.ShareUrl::class.java
            is ShareData.ShareText -> ShareData.ShareText::class.java
            is ShareData.ShareImage -> ShareData.ShareImage::class.java
            is ShareData.ShareImages -> ShareData.ShareImages::class.java
            is ShareData.ShareFile -> ShareData.ShareFile::class.java
            is ShareData.ShareCheckList -> ShareData.ShareCheckList::class.java
            is ShareData.ShareNotification -> ShareData.ShareNotification::class.java
        }
        return gson.toJson(shareData, clazz)
    }

    @TypeConverter
    fun stringToShareData(str: String): ShareData {
        val json = gson.fromJson(str, JsonObject::class.java)
        val clazz = when (enumByNameIgnoreCase(json.get("type").asString, ShareType.UNKNOWN)) {
            ShareType.URL -> ShareData.ShareUrl::class.java
            ShareType.TEXT -> ShareData.ShareText::class.java
            ShareType.IMAGE -> ShareData.ShareImage::class.java
            ShareType.IMAGES -> ShareData.ShareImages::class.java
            ShareType.FILE -> ShareData.ShareFile::class.java
            ShareType.CHECK_LIST -> ShareData.ShareCheckList::class.java
            ShareType.NOTIFICATION -> ShareData.ShareNotification::class.java
            else -> error("No share type this data $str")
        }
        return gson.fromJson(json, clazz)
    }
}