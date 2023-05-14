package com.dinhlam.sharebox.json

import android.net.Uri
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareType
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ShareImagesJsonSerializerDeserializer :
    JsonSerializer<ShareData.ShareImages>,
    JsonDeserializer<ShareData.ShareImages> {
    override fun serialize(
        src: ShareData.ShareImages,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        val jsonArray = JsonArray()
        src.uris.forEach { uri ->
            jsonArray.add(uri.toString())
        }
        jsonObject.addProperty("type", ShareType.IMAGES.type)
        jsonObject.add("data", jsonArray)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareData.ShareImages {
        val arrUri = json.asJsonObject.get("data").asJsonArray
        return ShareData.ShareImages(arrUri.map { str -> Uri.parse(str.asString) })
    }
}
