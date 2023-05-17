package com.dinhlam.sharebox.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.browser.customtabs.CustomTabsIntent
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.ui.home.HomeActivity
import com.dinhlam.sharebox.ui.home.bookmark.creator.BookmarkCollectionCreatorActivity
import com.dinhlam.sharebox.ui.passcode.PasscodeActivity
import com.dinhlam.sharebox.ui.setting.SettingActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRouter @Inject constructor(@ApplicationContext private val context: Context) {

    fun home(): Intent {
        return Intent(context, HomeActivity::class.java)
    }

    fun setting(): Intent {
        return Intent(context, SettingActivity::class.java)
    }

    fun moveToChromeCustomTab(context: Context, url: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabIntent = builder.build()
        customTabIntent.launchUrl(context, Uri.parse(url))
    }

    fun moveToBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun bookmarkCollectionCreatorIntent(context: Context): Intent {
        return Intent(context, BookmarkCollectionCreatorActivity::class.java)
    }

    fun pickImageIntent(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    fun passcodeIntent(context: Context): Intent {
        return Intent(context, PasscodeActivity::class.java)
    }

    fun passcodeIntent(context: Context, passcode: String): Intent {
        return Intent(context, PasscodeActivity::class.java).apply {
            putExtra(AppExtras.EXTRA_PASSCODE, passcode)
        }
    }
}
