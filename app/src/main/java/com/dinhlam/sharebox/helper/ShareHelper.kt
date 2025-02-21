package com.dinhlam.sharebox.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetDialogFragment
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.dialog.action.BottomSheetShareActionDialogFragment
import com.dinhlam.sharebox.dialog.download.DownloadFileDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.ifNotZero
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.extensions.isNetworkUrl
import com.dinhlam.sharebox.extensions.queryIntentActivitiesCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.storage.FirebaseStorageManager
import com.dinhlam.sharebox.ui.comment.CommentFragment
import com.dinhlam.sharebox.ui.sharereceive.ShareReceiveActivity
import com.dinhlam.sharebox.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val router: Router,
    private val firebaseStorageManager: FirebaseStorageManager
) {

    fun showMore(
        activity: FragmentActivity,
        share: ShareDetail,
        onBottomSheetDismissListener: BaseBottomSheetDialogFragment.OnBottomSheetDismissListener
    ) {
        BottomSheetShareActionDialogFragment.showDialog(
            activity.supportFragmentManager,
            share.shareId
        ).bottomSheetDismissListener = onBottomSheetDismissListener
    }

    fun shareToOther(activity: FragmentActivity, share: ShareDetail) {
        val intent = Intent(Intent.ACTION_SEND)
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> {
                intent.putExtra(Intent.EXTRA_TEXT, shareData.url)
                intent.type = "text/*"
            }

            is ShareData.ShareText -> {
                intent.putExtra(
                    Intent.EXTRA_TEXT, shareData.text
                )
                intent.type = "text/*"
            }

            is ShareData.ShareImage -> {
                if (shareData.uri.toString().isNetworkUrl()) {
                    return DownloadFileDialogFragment.showDialog(
                        activity.supportFragmentManager,
                        shareData.uri.toString(),
                        FileUtils.createFileName("image", "jpg"),
                        "image/jpg"
                    )
                } else {
                    intent.putExtra(
                        Intent.EXTRA_STREAM, shareData.uri
                    )
                    intent.setDataAndType(shareData.uri, "image/*")
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            }

            is ShareData.ShareImages -> {
                intent.action = Intent.ACTION_SEND_MULTIPLE
                intent.putParcelableArrayListExtra(
                    Intent.EXTRA_STREAM, arrayListOf(*shareData.uris.toTypedArray())
                )
                intent.setDataAndType(shareData.uris[0], "image/*")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            is ShareData.ShareFile -> {
                if (shareData.uri.toString().isNetworkUrl()) {
                    return DownloadFileDialogFragment.showDialog(
                        activity.supportFragmentManager,
                        shareData.uri.toString(),
                        shareData.fileName,
                        shareData.mimeType
                    )
                } else {
                    intent.putExtra(
                        Intent.EXTRA_STREAM, shareData.uri
                    )
                    intent.setDataAndType(shareData.uri, shareData.mimeType)
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            }

            is ShareData.ShareCheckList -> {
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    getShareCheckListText(share.shareNote, shareData)
                )
                intent.type = "text/*"
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val components = arrayOf(ComponentName(context, ShareReceiveActivity::class.java))
            val chooser = Intent.createChooser(intent, "Share To")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, components)
            context.startActivity(chooser)
        } else {
            val resolveInfoList = context.packageManager.queryIntentActivitiesCompat(intent, 0)
            if (resolveInfoList.isNotEmpty()) {
                val targetIntents = mutableListOf<Intent>()
                resolveInfoList.forEach { resolveInfo ->
                    val newIntent = Intent(intent)
                    if (!resolveInfo.activityInfo.packageName.equals(
                            BuildConfig.APPLICATION_ID, true
                        )
                    ) {
                        newIntent.setPackage(resolveInfo.activityInfo.packageName)
                        targetIntents.add(intent)
                    }
                }
                if (targetIntents.isEmpty()) {
                    return
                }

                val chooserIntent = Intent.createChooser(targetIntents.removeAt(0), "Share To")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            }
        }
    }

    fun copyShare(context: Context, share: ShareDetail) {
        when (val shareData = share.shareData) {
            is ShareData.ShareText -> context.copy(shareData.text)
            is ShareData.ShareUrl -> context.copy(shareData.url)
            is ShareData.ShareCheckList -> {
                val text = getShareCheckListText(share.shareNote, shareData)
                context.copy(text)
            }

            else -> context.showToast(R.string.nothing_to_copy)
        }
    }

    private fun getShareCheckListText(shareNote: String?, shareData: ShareData.ShareCheckList): String {
        return buildString {
            append("Check List [${shareNote.takeIfNotNullOrBlank() ?: "-"}]")
            append("\n\n")

            for (checklist in shareData.checkListDataList) {
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
    }

    fun viewShareImage(context: Context, uri: Uri) {
        context.startActivity(router.imageViewer(context, listOf(uri)))
    }

    fun viewShareImages(context: Context, uris: List<Uri>) {
        context.startActivity(router.imageViewer(context, uris))
    }

    fun showCommentDialog(fragmentManager: FragmentManager, shareId: String) {
        CommentFragment().apply {
            arguments = Bundle().apply {
                putString(AppExtras.EXTRA_SHARE_ID, shareId)
            }
        }.show(fragmentManager, "CommentFragment")
    }

    fun viewInSource(context: Context, videoSource: VideoSource, shareData: ShareData) {
        when (videoSource) {
            is VideoSource.Directly -> {}
            is VideoSource.Tiktok -> viewInTiktok(context, shareData)
            is VideoSource.Youtube -> viewInYoutube(context, shareData)
            is VideoSource.Facebook -> viewInFacebook(context, shareData)
        }
    }

    private fun viewInTiktok(context: Context, shareData: ShareData) {
        val shareUrl = shareData.cast<ShareData.ShareUrl>() ?: return
        val viewIntent = router.viewIntent(shareUrl.url)

        val resolveInfoList = context.packageManager?.queryIntentActivitiesCompat(
            viewIntent, PackageManager.GET_META_DATA
        ) ?: return

        resolveInfoList.run stop@{
            forEach { resolveInfo ->
                if (resolveInfo.activityInfo.packageName.equals(AppConsts.TIKTOK_M_PACKAGE_ID)) {
                    viewIntent.setPackage(AppConsts.TIKTOK_M_PACKAGE_ID)
                    return@stop
                }

                if (resolveInfo.activityInfo.packageName.equals(AppConsts.TIKTOK_O_PACKAGE_ID)) {
                    viewIntent.setPackage(AppConsts.TIKTOK_O_PACKAGE_ID)
                    return@stop
                }
            }
        }

        if (viewIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(viewIntent)
        } else {
            context.startActivity(router.playStoreIntent(AppConsts.TIKTOK_M_PACKAGE_ID))
        }
    }

    private fun viewInYoutube(context: Context, shareData: ShareData) {
        val shareUrl = shareData.cast<ShareData.ShareUrl>() ?: return
        val viewIntent = router.viewIntent(shareUrl.url)
        viewIntent.runCatching {
            context.startActivity(this)
        }.onFailure { error ->
            Logger.error(error)
            context.startActivity(router.playStoreIntent(AppConsts.YOUTUBE_M_PACKAGE_ID))
        }
    }

    private fun viewInFacebook(context: Context, shareData: ShareData) {
        val shareUrl = shareData.cast<ShareData.ShareUrl>() ?: return
        val viewIntent = router.viewIntent(shareUrl.url)
        viewIntent.setPackage(AppConsts.FACEBOOK_M_PACKAGE_ID)

        if (viewIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(viewIntent)
        } else {
            context.startActivity(router.playStoreIntent(AppConsts.FACEBOOK_M_PACKAGE_ID))
        }
    }

    fun isSupportDownloadLink(url: String): Boolean {
        return arrayOf(
            "tiktok.com",
            "youtube.com",
            "fb.com",
            "facebook.com",
            "you.tube"
        ).any { supportLink -> url.contains(supportLink, true) }
    }

    fun downloadShareContent(context: Context, share: ShareDetail) {
        val urls = when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> listOf(shareData.url)
            is ShareData.ShareImage -> listOf(shareData.uri.toString())
            is ShareData.ShareImages -> shareData.uris.map(Uri::toString)
            is ShareData.ShareFile -> listOf(shareData.uri.toString())
            else -> emptyList()
        }

        if (urls.isEmpty()) {
            context.showToast(R.string.nothing_to_download)
            return
        }

        context.startActivity(router.downloadBottomSheet(context, urls))
    }
}
