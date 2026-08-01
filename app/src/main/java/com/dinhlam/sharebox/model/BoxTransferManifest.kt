package com.dinhlam.sharebox.model

/**
 * Portable representation of a box stored in Firebase Storage.
 *
 * [shareData] uses the same JSON representation as Room. File and image URIs
 * point at exported Firebase assets until the manifest is imported.
 */
data class BoxTransferManifest(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val boxId: String,
    val boxName: String,
    val boxDesc: String?,
    val createdBy: String,
    val createdDate: Long,
    val exportedAt: Long,
    val shares: List<BoxTransferShare>,
) {
    init {
        require(schemaVersion == CURRENT_SCHEMA_VERSION) {
            "Unsupported box manifest schema version: $schemaVersion"
        }
        require(boxId.isNotBlank()) { "Box id cannot be blank" }
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
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
