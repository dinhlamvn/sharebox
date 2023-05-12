package com.dinhlam.sharebox.repository.mapper

import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.extensions.enumByNameIgnoreCase
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareType
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareToShareDataMapper @Inject constructor(
    private val gson: Gson
) {
    fun map(share: Share): ShareData {
        return when (val shareType = enumByNameIgnoreCase(share.shareType, ShareType.UNKNOWN)) {
            ShareType.URL -> gson.fromJson(share.shareData, ShareData.ShareUrl::class.java)

            ShareType.TEXT -> gson.fromJson(share.shareData, ShareData.ShareText::class.java)

            ShareType.IMAGE -> gson.fromJson(share.shareData, ShareData.ShareImage::class.java)

            ShareType.IMAGES -> gson.fromJson(share.shareData, ShareData.ShareImages::class.java)

            else -> error("Has error when parse share type $shareType")
        }
    }
}