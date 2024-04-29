package com.dinhlam.sharebox.extensions

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.listmodel.ListItemListModel
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.router.Router
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial

fun ShareDetail.buildListItemListModel(
    activity: AppCompatActivity,
    shareHelper: ShareHelper,
    router: Router
): BaseListAdapter.BaseListModel {


    fun getRecentlyIcon(shareData: ShareData): IIcon {
        return when (shareData) {
            is ShareData.ShareText -> FontAwesome.Icon.faw_sticky_note
            is ShareData.ShareUrl -> GoogleMaterial.Icon.gmd_web
            is ShareData.ShareImage -> FontAwesome.Icon.faw_image
            is ShareData.ShareImages -> FontAwesome.Icon.faw_images
        }
    }

    fun getRecentlyTitle(shareDetail: ShareDetail): String? {
        return when (val shareData = shareDetail.shareData) {
            is ShareData.ShareText -> shareData.text
            is ShareData.ShareUrl -> shareData.url
            is ShareData.ShareImage -> shareDetail.shareNote
            is ShareData.ShareImages -> shareDetail.shareNote
        }
    }

    fun getRecentlySubtitle(shareDetail: ShareDetail): String? {
        return when (val shareData = shareDetail.shareData) {
            is ShareData.ShareText -> "[${shareDetail.createdAt.format("yyyy MMM d HH:mm")}] ${shareDetail.shareNote}"
            is ShareData.ShareUrl -> shareDetail.shareNote
            is ShareData.ShareImage -> shareDetail.createdAt.format("yyyy MMM d HH:mm")
            is ShareData.ShareImages -> shareDetail.createdAt.format()
        }
    }

    fun openShare(share: ShareDetail) {
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> router.moveToBrowser(shareData.url)
            is ShareData.ShareText -> {
                shareHelper.openTextViewerDialog(activity, shareData.text)
            }

            is ShareData.ShareImage -> shareHelper.viewShareImage(
                activity, shareData.uri
            )

            is ShareData.ShareImages -> shareHelper.viewShareImages(
                activity, shareData.uris
            )
        }
    }


    return ListItemListModel(
        "share_${this.shareId}",
        getRecentlyIcon(this.shareData),
        getRecentlyTitle(this),
        getRecentlySubtitle(this),
        BaseListAdapter.NoHashProp(View.OnClickListener {
            shareHelper.showMore(activity, this)
        }),
        BaseListAdapter.NoHashProp(View.OnClickListener {
            openShare(this)
        })
    )
}