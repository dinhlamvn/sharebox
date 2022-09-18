package com.dinhlam.sharesaver.json

import com.dinhlam.sharesaver.ui.share.ShareData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ShareTextJsonSerializerDeserializer : JsonSerializer<ShareData.ShareInfo.ShareText>,
    JsonDeserializer<ShareData.ShareInfo.ShareText> {
    override fun serialize(
        src: ShareData.ShareInfo.ShareText,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("data", src.text)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareData.ShareInfo.ShareText {
        return ShareData.ShareInfo.ShareText(json.asJsonObject.get("data").asString)
    }
}