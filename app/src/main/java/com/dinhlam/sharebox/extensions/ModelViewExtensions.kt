package com.dinhlam.sharebox.extensions

import android.net.Uri
import android.view.View
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.listmodel.ImageListModel
import com.dinhlam.sharebox.listmodel.list.ListImagesListModel
import com.dinhlam.sharebox.listmodel.list.ShareImageListModel
import com.dinhlam.sharebox.listmodel.list.ShareTextListModel
import com.dinhlam.sharebox.listmodel.list.ShareUrlListModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.UserDetail

fun ShareData.buildShareListModel(
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
): BaseListAdapter.BaseListModel {
    return when (this) {
        is ShareData.ShareUrl -> {
            val shareData = this.castNonNull<ShareData.ShareUrl>()
            ShareUrlListModel(
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

        is ShareData.ShareText -> {
            val shareData = this.castNonNull<ShareData.ShareText>()
            ShareTextListModel(
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
            ShareImageListModel(
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
                ImageListModel(
                    uri,
                    height = screenHeight.times(0.5f).toInt(),
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        actionViewImages?.invoke(shareId, shareData.uris)
                    })
                )
            }

            ListImagesListModel(
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
                BaseListAdapter.NoHashProp(actionOpen),
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