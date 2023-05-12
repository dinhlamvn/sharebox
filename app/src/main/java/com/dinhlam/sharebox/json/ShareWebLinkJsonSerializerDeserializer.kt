package com.dinhlam.sharebox.json

import com.dinhlam.sharebox.model.ShareData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ShareWebLinkJsonSerializerDeserializer :
    JsonSerializer<ShareData.ShareUrl>,
    JsonDeserializer<ShareData.ShareUrl> {
    override fun serialize(
        src: ShareData.ShareUrl,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("data", src.url)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareData.ShareUrl {
        return ShareData.ShareUrl(json.asJsonObject.get("data").asString)
    }
}
