package com.dinhlam.sharebox.extensions

import androidx.annotation.DrawableRes
import com.dinhlam.sharebox.R
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
                shareDate,
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
                shareDate,
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
                shareDate,
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

fun Boolean.asBookmarkIcon(
    @DrawableRes iconOn: Int = R.drawable.ic_bookmarked,
    @DrawableRes iconOff: Int = R.drawable.ic_bookmark
): Int {
    return if (this) iconOn else iconOff
}