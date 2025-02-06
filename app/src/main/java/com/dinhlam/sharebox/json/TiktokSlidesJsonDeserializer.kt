package com.dinhlam.sharebox.json

import com.dinhlam.sharebox.data.network.response.AppDLResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object TiktokSlidesJsonDeserializer : JsonDeserializer<AppDLResponse.Slides> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AppDLResponse.Slides? {
        val jsonObject = json?.asJsonObject ?: return null
        val music = jsonObject.get("music")?.asString
        val imageKeySet = jsonObject.keySet().mapNotNull(String::toIntOrNull)
        val slideDataList = imageKeySet.map { imageKey ->
            val jsonImage = jsonObject.getAsJsonObject("$imageKey")
            val url = jsonImage.get("url").asString
            val width = jsonImage.get("width").asInt
            val height = jsonImage.get("height").asInt
            AppDLResponse.SlideData(url, width, height)
        }
        return AppDLResponse.Slides(slideDataList, music)
    }
}