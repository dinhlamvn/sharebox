package com.dinhlam.sharebox.extensions

import android.view.View
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.listmodel.ListItemListModel
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail

fun ShareDetail.buildListItemListModel(
    onShowMore: (ShareDetail) -> Unit = { },
    onOpenShare: (ShareDetail) -> Unit = { }
): BaseListAdapter.BaseListModel {

    fun getRecentlyIcon(shareData: ShareData): String {
        return when (shareData) {
            is ShareData.ShareText -> "f249"
            is ShareData.ShareUrl -> "f0c1"
            is ShareData.ShareImage -> "f03e"
            is ShareData.ShareImages -> "f302"
            is ShareData.ShareFile -> "f15c"
            is ShareData.ShareCheckList -> "f0ae"
        }
    }

    fun getRecentlyTitle(shareDetail: ShareDetail): String? {
        return when (val shareData = shareDetail.shareData) {
            is ShareData.ShareText -> shareData.text
            is ShareData.ShareUrl -> shareData.url
            is ShareData.ShareImage -> shareDetail.shareNote
            is ShareData.ShareImages -> shareDetail.shareNote
            is ShareData.ShareFile -> shareData.fileName
            is ShareData.ShareCheckList -> shareDetail.shareNote
        }
    }

    fun getRecentlySubtitle(shareDetail: ShareDetail): String? {
        return when (val shareData = shareDetail.shareData) {
            is ShareData.ShareText -> "${shareDetail.createdAt.format("yyyy MMM d HH:mm")} ${shareDetail.shareNote}"
            is ShareData.ShareUrl -> shareDetail.shareNote
            is ShareData.ShareImage -> shareDetail.createdAt.format("yyyy MMM d HH:mm")
            is ShareData.ShareImages -> shareDetail.createdAt.format()
            is ShareData.ShareFile -> shareData.fileSize.asHumanReadableSize()
            is ShareData.ShareCheckList -> shareData.checkListDataList.firstOrNull()?.title
        }
    }


    return ListItemListModel(
        "share_${this.shareId}",
        getRecentlyIcon(this.shareData),
        getRecentlyTitle(this),
        getRecentlySubtitle(this),
        BaseListAdapter.NoHashProp(View.OnClickListener {
            onShowMore(this)
        }),
        BaseListAdapter.NoHashProp(View.OnClickListener {
            onOpenShare(this)
        })
    )
}