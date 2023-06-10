package com.dinhlam.sharebox.data.model.realtimedb

import com.dinhlam.sharebox.data.local.entity.Box
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.getOrThrow
import com.google.firebase.database.PropertyName

data class RealtimeBoxObj(
    @get:PropertyName("id") val id: String,
    @get:PropertyName("name") val name: String,
    @get:PropertyName("desc") val desc: String?,
    @get:PropertyName("created_by") val createdBy: String,
    @get:PropertyName("created_date") val createdDate: Long,
    @get:PropertyName("passcode") val passcode: String?,
) {

    companion object {
        @JvmStatic
        fun from(box: Box): RealtimeBoxObj {
            return RealtimeBoxObj(
                box.boxId, box.boxName, box.boxDesc, box.createdBy, box.createdDate, box.passcode
            )
        }

        @JvmStatic
        fun from(jsonMap: Map<String, Any>): RealtimeBoxObj {
            val id = jsonMap.getOrThrow("id").castNonNull<String>()
            val name = jsonMap.getOrThrow("name").castNonNull<String>()
            val desc = jsonMap.getOrThrow("desc").cast<String>()
            val createdBy = jsonMap.getOrThrow("created_by").castNonNull<String>()
            val createdDate = jsonMap.getOrThrow("created_date").castNonNull<Long>()
            val passcode = jsonMap.getOrThrow("passcode").cast<String>()

            return RealtimeBoxObj(
                id, name, desc, createdBy, createdDate, passcode
            )
        }
    }
}
