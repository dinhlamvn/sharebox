package com.dinhlam.sharebox.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.dialog.viewimages.ViewImagesDialogFragment
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareDetail
import com.dinhlam.sharebox.ui.sharereceive.ShareReceiveActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @Suppress("DEPRECATION")
    fun shareToOther(share: ShareDetail) {
        val intent = Intent(Intent.ACTION_SEND)
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> {
                intent.putExtra(Intent.EXTRA_TEXT, shareData.castNonNull<ShareData.ShareUrl>().url)
                intent.type = "text/*"
            }

            is ShareData.ShareText -> {
                intent.putExtra(
                    Intent.EXTRA_TEXT, shareData.castNonNull<ShareData.ShareText>().text
                )
                intent.type = "text/*"
            }

            is ShareData.ShareImage -> {
                intent.putExtra(
                    Intent.EXTRA_STREAM, shareData.castNonNull<ShareData.ShareImage>().uri
                )
                intent.setDataAndType(shareData.uri, "image/*")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            is ShareData.ShareImages -> {
                val data = shareData.castNonNull<ShareData.ShareImages>()
                intent.action = Intent.ACTION_SEND_MULTIPLE
                intent.putParcelableArrayListExtra(
                    Intent.EXTRA_STREAM, arrayListOf(*data.uris.toTypedArray())
                )
                intent.setDataAndType(data.uris[0], "image/*")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val components = arrayOf(ComponentName(context, ShareReceiveActivity::class.java))
            val chooser = Intent.createChooser(intent, "Share To")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, components)
            context.startActivity(chooser)
        } else {
            val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
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

    fun viewShareImage(activity: FragmentActivity, uri: Uri) {
        ViewImagesDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(
                    ViewImagesDialogFragment.EXTRA_LIST_URI,
                    arrayListOf(uri)
                )
            }
        }.show(activity.supportFragmentManager, "ViewImagesDialogFragment")
    }

    fun viewShareImages(activity: FragmentActivity, uris: List<Uri>) {
        ViewImagesDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(
                    ViewImagesDialogFragment.EXTRA_LIST_URI,
                    arrayListOf(*uris.toTypedArray())
                )
            }
        }.show(activity.supportFragmentManager, "ViewImagesDialogFragment")
    }
}
