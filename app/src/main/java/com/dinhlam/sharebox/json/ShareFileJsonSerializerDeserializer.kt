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

object ShareFileJsonSerializerDeserializer :
    JsonSerializer<ShareData.ShareFile>,
    JsonDeserializer<ShareData.ShareFile> {
    override fun serialize(
        src: ShareData.ShareFile,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", ShareType.FILE.type)

        val fileObject = JsonObject()
        fileObject.addProperty("fileName", src.fileName)
        fileObject.addProperty("fileSize", src.fileSize)
        fileObject.addProperty("fileUri", src.uri.toString())
        jsonObject.add("data", fileObject)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareData.ShareFile {
        val fileObject = json.asJsonObject.get("data").asJsonObject
        val fileName = fileObject.get("fileName").asString
        val fileSize = fileObject.get("fileSize").asDouble
        val fileUri = fileObject.get("fileUri").asString
        return ShareData.ShareFile(fileName, fileSize, Uri.parse(fileUri))
    }
}
