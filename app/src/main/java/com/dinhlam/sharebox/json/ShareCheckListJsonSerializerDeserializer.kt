package com.dinhlam.sharebox.json

import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareType
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object ShareCheckListJsonSerializerDeserializer :
    JsonSerializer<ShareData.ShareCheckList>,
    JsonDeserializer<ShareData.ShareCheckList> {
    override fun serialize(
        src: ShareData.ShareCheckList,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", ShareType.CHECK_LIST.type)

        val jsonArray = JsonArray()
        src.checkListDataList.forEach { checkListData ->
            val checkListObj = JsonObject()
            checkListObj.addProperty("title", checkListData.title)
            checkListObj.addProperty("done", checkListData.done)
            checkListObj.addProperty("datetime", checkListData.datetime)
            checkListObj.addProperty("reminder", checkListData.reminder)
            checkListObj.addProperty("updated_at", checkListData.updatedAt)
            jsonArray.add(checkListObj)
        }

        jsonObject.add("data", jsonArray)
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareData.ShareCheckList {
        val jsonObj = json.asJsonObject
        val checkListArray = jsonObj.get("data").asJsonArray

        val checkList = checkListArray.map { jsonElement ->
            val checkListObj = jsonElement.asJsonObject
            val title = checkListObj.get("title").asString
            val done = checkListObj.get("done").asBoolean
            val datetime = checkListObj.get("datetime").asLong
            val reminder = checkListObj.get("reminder").asLong
            val updatedAt = checkListObj.get("updated_at")?.asLong
            ShareData.ShareCheckList.CheckListData(title, done, datetime, reminder, updatedAt ?: 0L)
        }

        return ShareData.ShareCheckList(checkList)
    }
}
