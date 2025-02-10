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
import com.dinhlam.sharebox.extensions.getColorCompat
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.model.DownloadData
import com.dinhlam.sharebox.receiver.CustomTabsDownloadBroadcastReceiver
import com.dinhlam.sharebox.receiver.CustomTabsShareBroadcastReceiver
import com.dinhlam.sharebox.ui.bookmark.BookmarkActivity
import com.dinhlam.sharebox.ui.bookmark.form.BookmarkCollectionFormActivity
import com.dinhlam.sharebox.ui.bookmark.list.BookmarkListItemActivity
import com.dinhlam.sharebox.ui.boxdetail.BoxDetailActivity
import com.dinhlam.sharebox.ui.boxform.BoxFormActivity
import com.dinhlam.sharebox.ui.boxinvited.BoxInvitedActivity
import com.dinhlam.sharebox.ui.boxlist.BoxListActivity
import com.dinhlam.sharebox.ui.boxmember.BoxMemberActivity
import com.dinhlam.sharebox.ui.clipboard.ClipboardActivity
import com.dinhlam.sharebox.ui.downloadpopup.DownloadPopupActivity
import com.dinhlam.sharebox.ui.home.HomeFragment
import com.dinhlam.sharebox.ui.imageviewer.ImageViewerActivity
import com.dinhlam.sharebox.ui.passcode.PasscodeActivity
import com.dinhlam.sharebox.ui.profile.ProfileFragment
import com.dinhlam.sharebox.ui.setting.SettingActivity
import com.dinhlam.sharebox.ui.setting.SettingComposeActivity
import com.dinhlam.sharebox.ui.sharelink.ShareLinkActivity
import com.dinhlam.sharebox.ui.signin.SignInActivity
import com.dinhlam.sharebox.ui.textinput.TextInputActivity
import com.dinhlam.sharebox.ui.trash.TrashActivity
import com.dinhlam.sharebox.utils.Icons

class AppRouter constructor(private val context: Context) : Router {

    override fun home(isNewTask: Boolean): Intent {
        return Intent(context, HomeFragment::class.java).apply {
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

    override fun moveToChromeCustomTab(
        context: Context, url: String, boxId: String?, boxName: String?, supportDownload: Boolean
    ) {
        val downloadDesc = context.getString(R.string.download_content)
        val broadcastReceiverIntent = Intent(
            context,
            CustomTabsShareBroadcastReceiver::class.java
        ).putExtra(AppExtras.EXTRA_BOX_ID, boxId)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            CustomTabsShareBroadcastReceiver.REQUEST_CODE,
            broadcastReceiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val remoteViews = RemoteViews(context.packageName, R.layout.custom_tab_bottom_toolbar)
        remoteViews.setImageViewBitmap(R.id.image_box, Icons.boxIcon(context) {
            copy(colorRes = R.color.md_theme_primary)
        }.toBitmap())

        remoteViews.setTextViewText(
            R.id.text_box_name, boxName
        )

        remoteViews.setTextColor(
            R.id.text_box_name, context.getColorCompat(R.color.md_theme_primary)
        )

        remoteViews.setImageViewBitmap(R.id.image_archive, Icons.archiveIcon(context) {
            copy(colorRes = R.color.md_theme_primary, sizeDp = 24)
        }.toBitmap())

        val clickableIds = intArrayOf(R.id.image_archive)

        val downloadBroadcastReceiverIntent =
            Intent(context, CustomTabsDownloadBroadcastReceiver::class.java)

        val downloadPendingIntent = PendingIntent.getBroadcast(
            context,
            CustomTabsDownloadBroadcastReceiver.REQUEST_CODE,
            downloadBroadcastReceiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val customTabsIntent =
            CustomTabsIntent.Builder()
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .setSecondaryToolbarViews(remoteViews, clickableIds, pendingIntent)
                .apply {
                    if (supportDownload) {
                        setActionButton(
                            Icons.downloadIcon(context).toBitmap(),
                            downloadDesc,
                            downloadPendingIntent
                        )
                    }
                }
                .build()

        customTabsIntent.intent.setPackage("com.android.chrome")
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        customTabsIntent.launchUrl(context, Uri.parse(url))
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
            putExtra(AppExtras.EXTRA_PASSCODE, desc)
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

    override fun boxForm(context: Context, boxId: String?): Intent {
        return Intent(context, BoxFormActivity::class.java)
            .putExtra(AppExtras.EXTRA_BOX_ID, boxId)
    }

    override fun setting(): Intent {
        return Intent(context, SettingActivity::class.java)
    }

    override fun settingCompose(): Intent {
        return Intent(context, SettingComposeActivity::class.java)
    }

    override fun boxDetail(context: Context, boxId: String, isFromInvited: Boolean): Intent {
        return Intent(context, BoxDetailActivity::class.java)
            .putExtra(AppExtras.EXTRA_BOX_ID, boxId)
            .putExtra(AppExtras.EXTRA_BOOLEAN, isFromInvited)
    }

    override fun profile(context: Context): Intent {
        return Intent(context, ProfileFragment::class.java)
    }

    override fun textInput(
        context: Context,
        title: String?,
        text: String?,
        isEdit: Boolean
    ): Intent {
        return Intent(context, TextInputActivity::class.java)
            .putExtra(AppExtras.EXTRA_TITLE, title)
            .putExtra(Intent.EXTRA_TEXT, text)
            .putExtra(AppExtras.EXTRA_BOOLEAN, isEdit)
    }

    override fun shareLink(context: Context, uri: Uri?): Intent {
        return Intent(context, ShareLinkActivity::class.java).setData(uri)
    }

    override fun downloadPopup(
        context: Context,
        url: String,
        videos: List<DownloadData>,
        audios: List<DownloadData>,
        images: List<DownloadData>,
        notificationId: Int
    ): Intent {
        return Intent(
            context, DownloadPopupActivity::class.java
        ).putExtra(
            AppExtras.EXTRA_URL, url
        ).putParcelableArrayListExtra(
            AppExtras.EXTRA_DOWNLOAD_VIDEOS, arrayListOf(*videos.toTypedArray())
        ).putParcelableArrayListExtra(
            AppExtras.EXTRA_DOWNLOAD_AUDIOS, arrayListOf(*audios.toTypedArray())
        ).putParcelableArrayListExtra(
            AppExtras.EXTRA_DOWNLOAD_IMAGES, arrayListOf(*images.toTypedArray())
        ).putExtra(AppExtras.EXTRA_NOTIFICATION_ID, notificationId)

            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    override fun bookmark(context: Context): Intent {
        return Intent(context, BookmarkActivity::class.java)
    }

    override fun imageViewer(context: Context, uris: List<Uri>): Intent {
        return Intent(context, ImageViewerActivity::class.java)
            .putExtra(AppExtras.EXTRA_IMAGE_URIS, arrayListOf(*uris.toTypedArray()))
    }

    override fun boxList(context: Context, title: String?): Intent {
        return Intent(context, BoxListActivity::class.java)
            .putExtra(AppExtras.EXTRA_TITLE, title)
    }

    override fun trash(context: Context): Intent {
        return Intent(context, TrashActivity::class.java)
    }

    override fun boxMembers(context: Context, boxId: String): Intent {
        return Intent(context, BoxMemberActivity::class.java).putExtra(
            AppExtras.EXTRA_BOX_ID,
            boxId
        )
    }

    override fun boxInvited(context: Context): Intent {
        return Intent(context, BoxInvitedActivity::class.java)
    }

    override fun clipboard(context: Context): Intent {
        return Intent(context, ClipboardActivity::class.java)
    }

    override fun pickFile(context: Context): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        return intent
    }
}
