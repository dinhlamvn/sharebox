package com.dinhlam.sharebox.json

import android.net.Uri
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareType
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ShareImageJsonSerializerDeserializer :
    JsonSerializer<ShareData.ShareImage>,
    JsonDeserializer<ShareData.ShareImage> {
    override fun serialize(
        src: ShareData.ShareImage,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", ShareType.IMAGE.type)
        jsonObject.addProperty("data", src.uri.toString())
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareData.ShareImage {
        val uriStr = json.asJsonObject.get("data").asString
        return ShareData.ShareImage(Uri.parse(uriStr))
    }
}
