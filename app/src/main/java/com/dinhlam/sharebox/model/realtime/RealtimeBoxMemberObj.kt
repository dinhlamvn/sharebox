package com.dinhlam.sharebox.model.realtime

import com.google.firebase.database.PropertyName

data class RealtimeBoxMemberObj(
    @get:PropertyName("member_id") val memberId: String,
    @get:PropertyName("member_email") val memberEmail: String,
    @get:PropertyName("invited_by") val invitedBy: String,
    @get:PropertyName("added_at") val addedAt: Long,
)
