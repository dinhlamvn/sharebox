package com.dinhlam.sharebox.router

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.RemoteViews
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.drawable.toBitmap
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.receiver.ShareBroadcastReceiver
import com.dinhlam.sharebox.ui.bookmark.form.BookmarkCollectionFormActivity
import com.dinhlam.sharebox.ui.bookmark.list.BookmarkListItemActivity
import com.dinhlam.sharebox.ui.boxcreate.BoxCreateActivity
import com.dinhlam.sharebox.ui.boxdetail.BoxDetailActivity
import com.dinhlam.sharebox.ui.home.HomeActivity
import com.dinhlam.sharebox.ui.passcode.PasscodeActivity
import com.dinhlam.sharebox.ui.profile.ProfileActivity
import com.dinhlam.sharebox.ui.setting.SettingActivity
import com.dinhlam.sharebox.ui.signin.SignInActivity
import com.dinhlam.sharebox.utils.Icons

class AppRouter constructor(private val context: Context) : Router {

    override fun home(isNewTask: Boolean): Intent {
        return Intent(context, HomeActivity::class.java).apply {
            if (isNewTask) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        }
    }

    override fun signIn(signInForResult: Boolean): Intent {
        return Intent(
            context, SignInActivity::class.java
        ).putExtra(AppExtras.EXTRA_SIGN_IN_FOR_RESULT, signInForResult)
    }

    override fun moveToChromeCustomTab(context: Context, url: String, boxDetail: BoxDetail?) {
        val shareDesc = context.getString(R.string.archives)
        val shareBitmap = Icons.archiveIcon(context) {
            copy(colorRes = android.R.color.black)
        }.toBitmap()
        val broadcastReceiverIntent = Intent(context, ShareBroadcastReceiver::class.java)
            .putExtra(AppExtras.EXTRA_BOX_ID, boxDetail?.boxId)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ShareBroadcastReceiver.REQUEST_CODE,
            broadcastReceiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val remoteViews = RemoteViews(context.packageName, R.layout.custom_tab_bottom_toolbar)
        remoteViews.setImageViewBitmap(R.id.image_box, Icons.boxIcon(context) {
            copy(colorRes = android.R.color.black)
        }.toBitmap())
        remoteViews.setTextViewText(
            R.id.text_box_name,
            boxDetail?.boxName ?: context.getString(R.string.box_general)
        )

        CustomTabsIntent.Builder().setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .setSecondaryToolbarViews(remoteViews, null, null)
            .setColorScheme(CustomTabsIntent.COLOR_SCHEME_LIGHT)
            .setActionButton(shareBitmap, shareDesc, pendingIntent).build()
            .launchUrl(context, Uri.parse(url))
    }

    override fun moveToBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun bookmarkCollectionFormIntent(context: Context): Intent {
        return Intent(context, BookmarkCollectionFormActivity::class.java)
    }

    override fun bookmarkCollectionFormIntent(
        context: Context, bookmarkCollection: BookmarkCollectionDetail
    ): Intent {
        return Intent(context, BookmarkCollectionFormActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_BOOKMARK_COLLECTION, bookmarkCollection)
        }
    }

    override fun bookmarkListItemIntent(context: Context, bookmarkCollectionId: String): Intent {
        return Intent(context, BookmarkListItemActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_BOOKMARK_COLLECTION_ID, bookmarkCollectionId)
        }
    }

    override fun pickImageIntent(isMultiple: Boolean): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            if (isMultiple) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
    }

    override fun passcodeIntent(context: Context, desc: String?): Intent {
        return Intent(context, PasscodeActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_PASSCODE_DESCRIPTION, desc)
        }
    }

    override fun passcodeIntent(context: Context, passcode: String, desc: String?): Intent {
        return Intent(context, PasscodeActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_PASSCODE, passcode)
            putExtra(AppExtras.EXTRA_PASSCODE_DESCRIPTION, desc)
        }
    }

    override fun passcodeIntent(
        context: Context, passcode: String, extras: Bundle, desc: String?
    ): Intent {
        return Intent(context, PasscodeActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_PASSCODE, passcode)
            putExtra(AppExtras.EXTRA_PASSCODE_DESCRIPTION, desc)
            putExtras(extras)
        }
    }

    override fun viewIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    override fun playStoreIntent(packageName: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=$packageName"
            )
            setPackage("com.android.vending")
        }
    }

    override fun boxIntent(context: Context): Intent {
        return Intent(context, BoxCreateActivity::class.java)
    }

    override fun settingIntent(): Intent {
        return Intent(context, SettingActivity::class.java)
    }

    override fun boxDetail(context: Context, boxId: String): Intent {
        return Intent(context, BoxDetailActivity::class.java).putExtra(
            AppExtras.EXTRA_BOX_ID, boxId
        )
    }

    override fun profile(context: Context): Intent {
        return Intent(context, ProfileActivity::class.java)
    }
}
