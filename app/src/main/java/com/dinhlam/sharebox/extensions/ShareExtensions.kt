package com.dinhlam.sharebox.extensions

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.dinhlam.sharebox.dialog.download.DownloadFileDialogFragment
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.router.Router

fun Context.openShare(
    fragmentManager: FragmentManager,
    shareDetail: ShareDetail,
    router: Router,
    shareHelper: ShareHelper
) {
    when (val shareData = shareDetail.shareData) {
        is ShareData.ShareUrl -> router.moveToChromeCustomTab(
            this,
            shareData.url,
            shareDetail.boxDetail?.boxId,
            shareDetail.boxDetail?.boxName
        )

        is ShareData.ShareText -> {
            TextViewerDialogFragment().apply {
                arguments = bundleOf(Intent.EXTRA_TEXT to shareData.text)
            }.show(fragmentManager, "dialog_text_viewer")
        }

        is ShareData.ShareImage -> shareHelper.viewShareImage(
            this, shareData.uri
        )

        is ShareData.ShareImages -> shareHelper.viewShareImages(
            this, shareData.uris
        )

        is ShareData.ShareFile -> {
            val downloadUrl = shareData.uri.toString()
            DownloadFileDialogFragment.showDialog(
                fragmentManager,
                downloadUrl,
                shareData.fileName,
                shareData.mimeType
            )
        }

        is ShareData.ShareCheckList -> {
            startActivity(router.checkList(this, shareDetail.shareId))
        }
    }
}