package com.dinhlam.sharebox.model

import com.google.gson.annotations.SerializedName

data class BoxInvitedData(
    @SerializedName("boxId") val boxId: String,
    @SerializedName("boxName") val boxName: String,
    @SerializedName("invited_by") val invitedBy: String,
    @SerializedName("added_at") val addedAt: Long,
)