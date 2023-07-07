package com.dinhlam.sharebox.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.browser.customtabs.CustomTabsIntent
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.ui.box.BoxActivity
import com.dinhlam.sharebox.ui.home.HomeActivity
import com.dinhlam.sharebox.ui.home.bookmark.form.BookmarkCollectionFormActivity
import com.dinhlam.sharebox.ui.home.bookmark.list.BookmarkListItemActivity
import com.dinhlam.sharebox.ui.passcode.PasscodeActivity
import com.dinhlam.sharebox.ui.setting.SettingActivity
import com.dinhlam.sharebox.ui.signin.SignInActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRouter @Inject constructor(@ApplicationContext private val context: Context) {

    fun home(isNewTask: Boolean = false): Intent {
        return Intent(context, HomeActivity::class.java).apply {
            if (isNewTask) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        }
    }

    fun signIn(signInForResult: Boolean = false): Intent {
        return Intent(context, SignInActivity::class.java)
            .putExtra(AppExtras.EXTRA_SIGN_IN_FOR_RESULT, signInForResult)
    }

    fun moveToChromeCustomTab(context: Context, url: String) {
        val customTabIntent = CustomTabsIntent.Builder().build()
        customTabIntent.launchUrl(context, Uri.parse(url))
    }

    fun moveToBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun bookmarkCollectionFormIntent(context: Context): Intent {
        return Intent(context, BookmarkCollectionFormActivity::class.java)
    }

    fun bookmarkCollectionFormIntent(
        context: Context, bookmarkCollection: BookmarkCollectionDetail
    ): Intent {
        return Intent(context, BookmarkCollectionFormActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_BOOKMARK_COLLECTION, bookmarkCollection)
        }
    }

    fun bookmarkListItemIntent(context: Context, bookmarkCollectionId: String): Intent {
        return Intent(context, BookmarkListItemActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_BOOKMARK_COLLECTION_ID, bookmarkCollectionId)
        }
    }

    fun pickImageIntent(isMultiple: Boolean = false): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            if (isMultiple) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
    }

    fun passcodeIntent(context: Context, desc: String? = null): Intent {
        return Intent(context, PasscodeActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_PASSCODE_DESCRIPTION, desc)
        }
    }

    fun passcodeIntent(context: Context, passcode: String, desc: String? = null): Intent {
        return Intent(context, PasscodeActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_PASSCODE, passcode)
            putExtra(AppExtras.EXTRA_PASSCODE_DESCRIPTION, desc)
        }
    }

    fun viewIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    fun playStoreIntent(packageName: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=$packageName"
            )
            setPackage("com.android.vending")
        }
    }

    fun boxIntent(context: Context): Intent {
        return Intent(context, BoxActivity::class.java)
    }

    fun settingIntent(): Intent {
        return Intent(context, SettingActivity::class.java)
    }
}
