package com.dinhlam.sharesaver.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.dinhlam.sharesaver.BuildConfig
import com.dinhlam.sharesaver.database.entity.Share
import com.dinhlam.sharesaver.ui.share.ShareActivity
import com.dinhlam.sharesaver.ui.share.ShareData
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ShareHelper @Inject constructor(
    @ApplicationContext private val context: Context, private val gson: Gson
) {

    @Suppress("DEPRECATION")
    fun shareToOther(share: Share) {
        val intent = Intent(Intent.ACTION_SEND)
        when (share.shareType) {
            "web-link" -> {
                val shareData =
                    gson.fromJson(share.shareInfo, ShareData.ShareInfo.ShareWebLink::class.java)
                intent.putExtra(Intent.EXTRA_TEXT, shareData.url)
                intent.type = "text/*"
            }
            "text" -> {
                val shareData =
                    gson.fromJson(share.shareInfo, ShareData.ShareInfo.ShareText::class.java)
                intent.putExtra(Intent.EXTRA_TEXT, shareData.text)
                intent.type = "text/*"
            }
            "image" -> {
                val shareData =
                    gson.fromJson(share.shareInfo, ShareData.ShareInfo.ShareImage::class.java)
                intent.putExtra(Intent.EXTRA_STREAM, shareData.uri)
                intent.setDataAndType(shareData.uri, "image/*")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val components = arrayOf(ComponentName(context, ShareActivity::class.java))
            val chooser = Intent.createChooser(intent, "Share To")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, components)
            context.startActivity(chooser)
        } else {

            val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(
                    intent, PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                context.packageManager.queryIntentActivities(intent, 0)
            }
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
}