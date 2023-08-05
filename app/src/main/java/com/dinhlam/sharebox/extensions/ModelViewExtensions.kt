package com.dinhlam.sharebox.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.modelview.ImageModelView
import com.dinhlam.sharebox.modelview.grid.GridUrlModelView
import com.dinhlam.sharebox.modelview.list.ListImageModelView
import com.dinhlam.sharebox.modelview.list.ListImagesModelView
import com.dinhlam.sharebox.modelview.list.ListTextModelView
import com.dinhlam.sharebox.modelview.list.ListUrlModelView
import com.dinhlam.sharebox.utils.Icons

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
    boxDetail: BoxDetail?,
    actionOpen: Function1<String, Unit>? = null,
    actionShareToOther: Function1<String, Unit>? = null,
    actionLike: Function1<String, Unit>? = null,
    actionComment: Function1<String, Unit>? = null,
    actionBookmark: Function1<String, Unit>? = null,
    actionViewImage: ((String, Uri) -> Unit)? = null,
    actionViewImages: Function2<String, List<Uri>, Unit>? = null,
    actionBoxClick: Function1<BoxDetail?, Unit>? = null,
    useGrid: Boolean = false
): BaseListAdapter.BaseModelView {
    return when (this) {
        is ShareData.ShareUrl -> {
            val shareData = this.castNonNull<ShareData.ShareUrl>()

            if (useGrid) {
                GridUrlModelView(
                    shareId,
                    shareData.url,
                    shareDate,
                    shareNote,
                    likeNumber,
                    commentNumber,
                    user,
                    bookmarked,
                    liked,
                    boxDetail,
                    BaseListAdapter.NoHashProp(actionOpen),
                    BaseListAdapter.NoHashProp(actionShareToOther),
                    BaseListAdapter.NoHashProp(actionLike),
                    BaseListAdapter.NoHashProp(actionComment),
                    BaseListAdapter.NoHashProp(actionBookmark),
                    BaseListAdapter.NoHashProp(actionBoxClick),
                )
            } else {
                ListUrlModelView(
                    shareId,
                    shareData.url,
                    shareDate,
                    shareNote,
                    likeNumber,
                    commentNumber,
                    user,
                    bookmarked,
                    liked,
                    boxDetail,
                    BaseListAdapter.NoHashProp(actionOpen),
                    BaseListAdapter.NoHashProp(actionShareToOther),
                    BaseListAdapter.NoHashProp(actionLike),
                    BaseListAdapter.NoHashProp(actionComment),
                    BaseListAdapter.NoHashProp(actionBookmark),
                    BaseListAdapter.NoHashProp(actionBoxClick),
                )
            }
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
                boxDetail,
                BaseListAdapter.NoHashProp(actionOpen),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionLike),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
                BaseListAdapter.NoHashProp(actionBoxClick),
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
                boxDetail,
                BaseListAdapter.NoHashProp(null),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionLike),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
                BaseListAdapter.NoHashProp(actionViewImage),
                BaseListAdapter.NoHashProp(actionBoxClick),
            )
        }

        is ShareData.ShareImages -> {
            val shareData = this.castNonNull<ShareData.ShareImages>()

            val modelViews = shareData.uris.map { uri ->
                ImageModelView(
                    uri,
                    height = screenHeight.times(0.5f).toInt(),
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        actionViewImages?.invoke(shareId, shareData.uris)
                    })
                )
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
                boxDetail,
                BaseListAdapter.NoHashProp(null),
                BaseListAdapter.NoHashProp(actionShareToOther),
                BaseListAdapter.NoHashProp(actionLike),
                BaseListAdapter.NoHashProp(actionComment),
                BaseListAdapter.NoHashProp(actionBookmark),
                BaseListAdapter.NoHashProp(actionViewImages),
                BaseListAdapter.NoHashProp(actionBoxClick),
            )
        }
    }
}

fun Boolean.asBookmarkIcon(context: Context): Drawable {
    return if (this) Icons.bookmarkedIcon(context) else Icons.bookmarkIcon(context)
}

fun Boolean.asLikeIcon(context: Context): Drawable {
    return if (this) Icons.likedIcon(context) else Icons.likeIcon(context)
}