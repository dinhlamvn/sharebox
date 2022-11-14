package com.dinhlam.sharesaver.json

import android.net.Uri
import com.dinhlam.sharesaver.ui.share.ShareState
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ShareImageJsonSerializerDeserializer :
    JsonSerializer<ShareState.ShareInfo.ShareImage>,
    JsonDeserializer<ShareState.ShareInfo.ShareImage> {
    override fun serialize(
        src: ShareState.ShareInfo.ShareImage,
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
    ): ShareState.ShareInfo.ShareImage {
        val uriStr = json.asJsonObject.get("data").asString
        return ShareState.ShareInfo.ShareImage(Uri.parse(uriStr))
    }
}
