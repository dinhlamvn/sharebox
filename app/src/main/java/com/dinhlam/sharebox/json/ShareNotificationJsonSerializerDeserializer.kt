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

object ShareNotificationJsonSerializerDeserializer :
    JsonSerializer<ShareData.ShareNotification>,
    JsonDeserializer<ShareData.ShareNotification> {
    override fun serialize(
        src: ShareData.ShareNotification,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", ShareType.NOTIFICATION.type)
        jsonObject.add("data", JsonObject().apply {
            addProperty("app_name", src.appName)
            addProperty("title", src.title)
            addProperty("content", src.content)
            addProperty("deeplink", src.deeplink)
        })
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareData.ShareNotification {
        val jsonObj = json.asJsonObject.getAsJsonObject("data")
        return ShareData.ShareNotification(
            jsonObj.get("app_name").asString,
            jsonObj.get("title").asString,
            jsonObj.get("content").asString,
            jsonObj.get("deeplink")?.asString,
        )
    }
}
