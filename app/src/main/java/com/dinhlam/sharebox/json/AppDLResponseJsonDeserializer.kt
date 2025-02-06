package com.dinhlam.sharebox.json

import com.dinhlam.sharebox.data.network.response.AppDLResponse
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class AppDLResponseJsonDeserializer(gson: Gson) : JsonDeserializer<AppDLResponse> {

    private val appDLGson = gson.newBuilder().registerTypeAdapter(
        AppDLResponse.Slides::class.java,
        AppDLSlidesJsonDeserializer
    ).create()

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AppDLResponse {
        val jsonObject = json?.asJsonObject ?: error("The response is null")
        if (jsonObject.has("slides") && jsonObject.get("slides").isJsonPrimitive) {
            jsonObject.remove("slides")
        }
        return appDLGson.fromJson(jsonObject, typeOfT)
    }

    private object AppDLSlidesJsonDeserializer : JsonDeserializer<AppDLResponse.Slides> {
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
}