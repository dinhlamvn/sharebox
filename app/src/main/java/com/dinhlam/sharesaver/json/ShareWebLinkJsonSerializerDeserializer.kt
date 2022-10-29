package com.dinhlam.sharesaver.json

import com.dinhlam.sharesaver.ui.share.ShareState
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ShareWebLinkJsonSerializerDeserializer : JsonSerializer<ShareState.ShareInfo.ShareWebLink>,
    JsonDeserializer<ShareState.ShareInfo.ShareWebLink> {
    override fun serialize(
        src: ShareState.ShareInfo.ShareWebLink,
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
    ): ShareState.ShareInfo.ShareWebLink {
        return ShareState.ShareInfo.ShareWebLink(json.asJsonObject.get("data").asString)
    }
}