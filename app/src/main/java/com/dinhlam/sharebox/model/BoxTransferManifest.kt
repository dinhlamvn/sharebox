package com.dinhlam.sharebox.model

/** A complete portable snapshot. Asset URIs are relative ZIP entry names. */
data class BoxTransferManifest(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val boxId: String,
    val boxName: String,
    val boxDesc: String?,
    val createdBy: String,
    val createdDate: Long,
    val exportedAt: Long,
    val shares: List<BoxTransferShare>,
    val revisionId: String,
    val ancestors: List<String>,
    val assets: List<BoxTransferAsset>,
    // Returned by cloud export for sharing; not part of the portable package.
    val transferCode: String? = null,
) {
    companion object { const val CURRENT_SCHEMA_VERSION = 2 }
}

data class BoxTransferShare(
    val shareId: String,
    val shareUserId: String,
    val shareData: String,
    val isVideoShare: Boolean,
    val shareNote: String?,
    val shareDate: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

data class BoxTransferAsset(val path: String, val sha256: String, val size: Long)
