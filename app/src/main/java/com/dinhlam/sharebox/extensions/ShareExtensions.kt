package com.dinhlam.sharebox.extensions

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.dinhlam.sharebox.R
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
            TextViewerDialogFragment.showDialog(fragmentManager, shareData.text)
        }

        is ShareData.ShareImage -> shareHelper.viewShareImage(
            this, shareData.uri
        )

        is ShareData.ShareImages -> shareHelper.viewShareImages(
            this, shareData.uris
        )

        is ShareData.ShareFile -> {
            val intent = Intent(Intent.ACTION_VIEW, shareData.uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Open with"))
        }

        is ShareData.ShareCheckList -> {
            val text = buildString {
                append("<b>${getString(R.string.checklist)}</b>")
                append("\n")
                shareDetail.shareNote?.takeIfNotNullOrBlank()?.let { note ->
                    append(note)
                    append("\n")
                }

                for (checklist in shareData.checkListDataList) {
                    append("\n")
                    append("• <b>${getString(R.string.hint_input_check_list_title)}: </b>")
                    append(checklist.title)
                    append("\n")
                    append("• <b>${getString(R.string.datetime, "")}</b>")
                    append(
                        checklist.datetime.isNotZero.ifTrue(
                            checklist.datetime.format("dd MMM yyyy, HH:mm"),
                            "-"
                        )
                    )
                    append("\n")
                    append("• <b>${getString(R.string.status)}: </b>")
                    append(
                        checklist.done.ifTrue(
                            getString(R.string.done),
                            getString(R.string.not_done)
                        )
                    )
                    append("\n--------------------")
                }
            }
            TextViewerDialogFragment.showDialog(fragmentManager, text)
        }
    }
}