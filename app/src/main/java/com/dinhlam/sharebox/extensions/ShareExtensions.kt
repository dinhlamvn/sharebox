package com.dinhlam.sharebox.extensions

import android.content.Context
import androidx.fragment.app.FragmentManager
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
            val text = buildString {
                append("<b>Name: </b>")
                append(shareData.fileName)
                append("\n\n")
                append("<b>Size: </b>")
                append(shareData.fileSize.asHumanReadableSize())
            }
            TextViewerDialogFragment.showDialog(fragmentManager, text)
        }

        is ShareData.ShareCheckList -> {
            val text = buildString {
                append("<b>Check List</b> [${shareDetail.shareNote.takeIfNotNullOrBlank() ?: "-"}]")
                append("\n")

                for (checklist in shareData.checkListDataList) {
                    append("\n")
                    append("• Work title: ")
                    append(checklist.title)
                    append("\n")
                    append("• Deadline: ")
                    append(
                        checklist.datetime.ifNotZero.ifTrue(
                            checklist.datetime.format("dd MMM yyyy, HH:mm"),
                            "-"
                        )
                    )
                    append("\n")
                    append("• Status: ")
                    append(checklist.done.ifTrue("Done", "Not Done"))
                    append("\n--------------------")
                }
            }
            TextViewerDialogFragment.showDialog(fragmentManager, text)
        }
    }
}