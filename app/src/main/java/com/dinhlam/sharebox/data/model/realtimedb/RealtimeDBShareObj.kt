package com.dinhlam.sharebox.data.model.realtimedb

import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.extensions.cast
import com.google.firebase.database.PropertyName
import com.google.gson.Gson

data class RealtimeDBShareObj(
    @get:PropertyName("share_id")
    val shareId: String,
    @get:PropertyName("share_user_id")
    val shareUserId: String,
    @get:PropertyName("share_note")
    val shareNote: String?,
    @get:PropertyName("share_data")
    val shareData: String,
    @get:PropertyName("share_date")
    val shareDate: Long
) {

    companion object {
        @JvmStatic
        fun from(gson: Gson, share: Share): RealtimeDBShareObj {
            val shareDataStr = gson.toJson(share.shareData)
            return RealtimeDBShareObj(
                share.shareId,
                share.shareUserId,
                share.shareNote,
                shareDataStr,
                share.shareDate
            )
        }

        @JvmStatic
        fun from(jsonMap: Map<String, Any>): RealtimeDBShareObj {
            val shareId = jsonMap["share_id"]?.toString()
                ?: error("No share_id was found in the map $jsonMap")

            val shareUserId =
                jsonMap["share_user_id"]?.toString()
                    ?: error("No share_user_id was found in the map $jsonMap")

            val shareNote =
                jsonMap["share_note"]?.toString()
                    ?: error("No share_note was found in the map $jsonMap")

            val shareData =
                jsonMap["share_data"]?.toString()
                    ?: error("No share_data was found in the map $jsonMap")

            val shareDate =
                jsonMap["share_date"]?.cast<Long>()
                    ?: error("No share_date was found in the map $jsonMap")

            return RealtimeDBShareObj(
                shareId,
                shareUserId,
                shareNote,
                shareData,
                shareDate
            )
        }
    }
}
