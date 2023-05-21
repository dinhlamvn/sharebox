package com.dinhlam.sharebox.extensions

import android.view.View
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.modelview.ImageViewMoreModelView
import com.dinhlam.sharebox.modelview.list.ListImageModelView
import com.dinhlam.sharebox.modelview.list.ListImagesModelView
import com.dinhlam.sharebox.modelview.list.ListTextModelView
import com.dinhlam.sharebox.modelview.list.ListUrlModelView
import com.dinhlam.sharebox.utils.IconUtils

fun ShareData.buildShareModelViews(
    screenWidth: Int,
    shareId: String,
    createdAt: Long,
    shareNote: String?,
    user: UserDetail,
    shareVote: Int = 0,
    shareComment: Int = 0,
    bookmarked: Boolean = false,
    actionOpen: Function1<String, Unit>? = null,
    actionShareToOther: Function1<String, Unit>? = null,
    actionVote: Function1<String, Unit>? = null,
    actionComment: Function1<String, Unit>? = null,
    actionBookmark: Function1<String, Unit>? = null,
): BaseListAdapter.BaseModelView {
    return when (this) {
        is ShareData.ShareUrl -> {
            val shareData = this.castNonNull<ShareData.ShareUrl>()
            ListUrlModelView(
                shareId,
                IconUtils.getIconUrl(shareData.url),
                shareData.url,
                createdAt,
                shareNote,
                shareVote,
                shareComment,
                bookmarked,
                user,
                BaseListAdapter.NoHashProp(actionOpen),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionVote),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
            )
        }

        is ShareData.ShareText -> {
            val shareData = this.castNonNull<ShareData.ShareText>()
            ListTextModelView(
                shareId,
                IconUtils.getIconUrl(shareData.text),
                shareData.text,
                createdAt,
                shareNote,
                shareVote,
                shareComment,
                user,
                bookmarked,
                BaseListAdapter.NoHashProp(actionOpen),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionVote),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
            )
        }

        is ShareData.ShareImage -> {
            val shareData = this.castNonNull<ShareData.ShareImage>()
            ListImageModelView(
                shareId,
                shareData.uri,
                createdAt,
                shareNote,
                shareVote,
                shareComment,
                user,
                bookmarked,
                BaseListAdapter.NoHashProp(actionOpen),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionVote),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
            )
        }

        is ShareData.ShareImages -> {
            fun getSpanSize(size: Int, index: Int): Int {
                return when (size) {
                    AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT -> if (index < 2) 3 else 2
                    else -> 1
                }
            }

            fun getImageWidth(size: Int, index: Int): Int {
                return when (size) {
                    4 -> screenWidth.div(2)
                    AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT -> if (index < 2) screenWidth.div(2) else screenWidth.div(
                        3
                    )

                    else -> screenWidth.div(size)
                }
            }

            fun getNumber(realSize: Int, takeSize: Int, index: Int): Int {
                return when {
                    realSize > takeSize && index == takeSize - 1 -> realSize - takeSize
                    else -> 0
                }
            }

            val shareData = this.castNonNull<ShareData.ShareImages>()
            val pickItems = shareData.uris.take(AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT)

            val modelViews = pickItems.mapIndexed { index, uri ->
                ImageViewMoreModelView(
                    uri,
                    getSpanSize(pickItems.size, index),
                    getImageWidth(pickItems.size, index),
                    getImageWidth(pickItems.size, index),
                    getNumber(shareData.uris.size, pickItems.size, index),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        actionOpen?.invoke(shareId)
                    })
                )
            }

            val spanCount = when {
                shareData.uris.size == 4 -> 2
                shareData.uris.size >= AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT -> 6
                else -> shareData.uris.size
            }

            ListImagesModelView(
                shareId,
                shareData.uris,
                createdAt,
                shareNote,
                spanCount,
                modelViews,
                shareVote,
                shareComment,
                user,
                bookmarked,
                BaseListAdapter.NoHashProp(actionOpen),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionVote),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
            )
        }
    }
}