package com.dinhlam.sharebox.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.modelview.ImageModelView
import com.dinhlam.sharebox.modelview.list.ListImageModelView
import com.dinhlam.sharebox.modelview.list.ListImagesModelView
import com.dinhlam.sharebox.modelview.list.ListTextModelView
import com.dinhlam.sharebox.modelview.list.ListUrlModelView
import com.dinhlam.sharebox.utils.IconUtils

fun ShareData.buildShareModelViews(
    screenHeight: Int,
    shareId: String,
    shareDate: Long,
    shareNote: String?,
    user: UserDetail,
    likeNumber: Int = 0,
    commentNumber: Int = 0,
    bookmarked: Boolean = false,
    liked: Boolean = false,
    actionOpen: Function1<String, Unit>? = null,
    actionShareToOther: Function1<String, Unit>? = null,
    actionLike: Function1<String, Unit>? = null,
    actionComment: Function1<String, Unit>? = null,
    actionBookmark: Function1<String, Unit>? = null,
): BaseListAdapter.BaseModelView {
    return when (this) {
        is ShareData.ShareUrl -> {
            val shareData = this.castNonNull<ShareData.ShareUrl>()
            ListUrlModelView(
                shareId,
                shareData.url,
                shareDate,
                shareNote,
                likeNumber,
                commentNumber,
                bookmarked,
                liked,
                user,
                BaseListAdapter.NoHashProp(actionOpen),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionLike),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
            )
        }

        is ShareData.ShareText -> {
            val shareData = this.castNonNull<ShareData.ShareText>()
            ListTextModelView(
                shareId,
                shareData.text,
                shareDate,
                shareNote,
                likeNumber,
                commentNumber,
                user,
                bookmarked,
                liked,
                BaseListAdapter.NoHashProp(actionOpen),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionLike),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
            )
        }

        is ShareData.ShareImage -> {
            val shareData = this.castNonNull<ShareData.ShareImage>()
            ListImageModelView(
                shareId,
                shareData.uri,
                shareDate,
                shareNote,
                likeNumber,
                commentNumber,
                user,
                bookmarked,
                liked,
                BaseListAdapter.NoHashProp(actionOpen),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionLike),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
            )
        }

        is ShareData.ShareImages -> {
            val shareData = this.castNonNull<ShareData.ShareImages>()

            val modelViews = shareData.uris.map { uri ->
                ImageModelView(uri, height = screenHeight.times(0.5f).toInt())
            }

            ListImagesModelView(
                shareId,
                shareData.uris,
                shareDate,
                shareNote,
                modelViews,
                likeNumber,
                commentNumber,
                user,
                bookmarked,
                liked,
                BaseListAdapter.NoHashProp(actionOpen),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionLike),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
            )
        }
    }
}

fun Boolean.asBookmarkIcon(context: Context): Drawable {
    return if (this) IconUtils.bookmarkedIcon(context) else IconUtils.bookmarkIcon(context)
}

fun Boolean.asBookmarkIconLight(context: Context): Drawable {
    return if (this) IconUtils.bookmarkedIconLight(context) else IconUtils.bookmarkIconLight(context)
}

fun Boolean.asLikeIcon(context: Context): Drawable {
    return if (this) IconUtils.likedIcon(context) else IconUtils.likeIcon(context)
}

fun Boolean.asLikeIconLight(context: Context): Drawable {
    return if (this) IconUtils.likedIcon(context) else IconUtils.likeIconLight(context)
}