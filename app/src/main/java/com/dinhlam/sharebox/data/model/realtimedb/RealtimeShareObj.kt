package com.dinhlam.sharebox.data.model.realtimedb

import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.getOrThrow
import com.google.firebase.database.PropertyName
import com.google.gson.Gson

data class RealtimeShareObj(
    @get:PropertyName("share_id") val shareId: String,
    @get:PropertyName("share_user_id") val shareUserId: String,
    @get:PropertyName("share_note") val shareNote: String?,
    @get:PropertyName("share_data") val shareData: String,
    @get:PropertyName("share_box_id") val shareBoxId: String?,
    @get:PropertyName("share_date") val shareDate: Long
) {

    companion object {
        @JvmStatic
        fun from(gson: Gson, share: Share): RealtimeShareObj {
            val shareDataStr = gson.toJson(share.shareData)
            return RealtimeShareObj(
                share.shareId,
                share.shareUserId,
                share.shareNote,
                shareDataStr,
                share.shareBoxId,
                share.shareDate,
            )
        }

        @JvmStatic
        fun from(jsonMap: Map<String, Any>): RealtimeShareObj {
            val shareId = jsonMap.getOrThrow("share_id").castNonNull<String>()
            val shareUserId = jsonMap.getOrThrow("share_user_id").castNonNull<String>()
            val shareNote = jsonMap["share_note"]?.toString()
            val shareData = jsonMap.getOrThrow("share_data").castNonNull<String>()
            val shareBoxId = jsonMap["share_box_id"].cast<String>()
            val shareDate = jsonMap.getOrThrow("share_date").castNonNull<Long>()

            return RealtimeShareObj(
                shareId, shareUserId, shareNote, shareData, shareBoxId, shareDate
            )
        }
    }
}
