package com.dinhlam.sharebox.extensions

import android.content.Context
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.modelview.sharelist.ShareListImageModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListImagesModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListUrlModelView
import com.dinhlam.sharebox.ui.sharereceive.modelview.ShareReceiveImagesModelView
import com.dinhlam.sharebox.utils.IconUtils

fun ShareData.buildShareModelViews(
    context: Context,
    shareId: Int,
    createdAt: Long,
    shareNote: String?,
    user: UserDetail
): BaseListAdapter.BaseModelView? {
    return when (this) {
        is ShareData.ShareUrl -> {
            val shareData = this.castNonNull<ShareData.ShareUrl>()
            ShareListUrlModelView(
                id = "$shareId",
                iconUrl = IconUtils.getIconUrl(shareData.url),
                url = shareData.url,
                createdAt = createdAt,
                note = shareNote,
                shareId = shareId,
                userName = user.name,
                userAvatar = user.avatar,
                userLevel = user.level
            )
        }

        is ShareData.ShareText -> {
            val shareData = this.castNonNull<ShareData.ShareText>()
            ShareListTextModelView(
                id = "$shareId",
                iconUrl = IconUtils.getIconUrl(shareData.text),
                content = shareData.text,
                createdAt = createdAt,
                note = shareNote,
                shareId = shareId
            )
        }

        is ShareData.ShareImage -> {
            val shareData = this.castNonNull<ShareData.ShareImage>()
            ShareListImageModelView("$shareId", shareData.uri, createdAt, shareNote, shareId)
        }

        is ShareData.ShareImages -> {
            fun getSpanSize(size: Int, index: Int): Int {
                return when (size) {
                    AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT -> if (index < 2) 3 else 2
                    else -> 1
                }
            }

            fun getImageWidth(size: Int, index: Int): Int {
                val screenWidth = context.screenWidth()

                return when (size) {
                    4 -> screenWidth.div(2)
                    AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT -> if (index < 2) screenWidth.div(2) else screenWidth.div(
                        3
                    )

                    else -> screenWidth.div(size)
                }
            }

            fun getTextNumber(realSize: Int, takeSize: Int, index: Int): String {
                return when {
                    realSize > takeSize && index == takeSize - 1 -> "+${realSize - takeSize}"
                    else -> ""
                }
            }

            val shareData = this.castNonNull<ShareData.ShareImages>()
            val pickItems = shareData.uris.take(AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT)

            val modelViews = pickItems.mapIndexed { index, uri ->
                ShareReceiveImagesModelView(
                    "shareMultipleImage$uri",
                    uri,
                    getSpanSize(pickItems.size, index),
                    getImageWidth(pickItems.size, index),
                    getTextNumber(shareData.uris.size, pickItems.size, index)
                )
            }

            val spanCount = when {
                shareData.uris.size == 4 -> 2
                shareData.uris.size >= AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT -> 6
                else -> shareData.uris.size
            }

            ShareListImagesModelView(
                "$shareId",
                shareData.uris,
                createdAt,
                shareNote,
                shareId,
                spanCount,
                modelViews
            )
        }

        else -> null
    }
}