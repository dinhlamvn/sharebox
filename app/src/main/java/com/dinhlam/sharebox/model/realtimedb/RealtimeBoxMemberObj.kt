package com.dinhlam.sharebox.model.realtimedb

import com.google.firebase.database.PropertyName

data class RealtimeBoxMemberObj(
    @get:PropertyName("member_id") val memberId: String,
    @get:PropertyName("member_email") val memberEmail: String,
    @get:PropertyName("added_at") val addedAt: Long,
)
