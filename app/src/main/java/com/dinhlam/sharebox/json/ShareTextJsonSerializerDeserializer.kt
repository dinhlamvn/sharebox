package com.dinhlam.sharebox.json

import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareType
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ShareTextJsonSerializerDeserializer :
    JsonSerializer<ShareData.ShareText>,
    JsonDeserializer<ShareData.ShareText> {
    override fun serialize(
        src: ShareData.ShareText,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", ShareType.TEXT.type)
        jsonObject.addProperty("data", src.text)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareData.ShareText {
        return ShareData.ShareText(json.asJsonObject.get("data").asString)
    }
}
