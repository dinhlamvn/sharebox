package com.dinhlam.sharekeeper.json

import android.net.Uri
import com.dinhlam.sharekeeper.ui.share.ShareData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ShareImageJsonSerializerDeserializer : JsonSerializer<ShareData.ShareInfo.ShareImage>,
    JsonDeserializer<ShareData.ShareInfo.ShareImage> {
    override fun serialize(
        src: ShareData.ShareInfo.ShareImage,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("data", src.uri.toString())
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareData.ShareInfo.ShareImage {
        val uriStr = json.asJsonObject.get("data").asString
        return ShareData.ShareInfo.ShareImage(Uri.parse(uriStr))
    }
}