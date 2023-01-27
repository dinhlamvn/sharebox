package com.dinhlam.sharebox.router

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.dinhlam.sharebox.ui.home.HomeActivity
import com.dinhlam.sharebox.ui.list.ShareListActivity
import com.dinhlam.sharebox.ui.setting.SettingActivity
import com.dinhlam.sharebox.utils.ExtraUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppRouter @Inject constructor(@ApplicationContext private val context: Context) {

    fun home(): Intent {
        return Intent(context, HomeActivity::class.java)
    }

    fun setting(): Intent {
        return Intent(context, SettingActivity::class.java)
    }

    fun shareList(folderId: String): Intent {
        return Intent(context, ShareListActivity::class.java).apply {
            putExtra(ExtraUtils.EXTRA_FOLDER_ID, folderId)
        }
    }

    fun shareListSearch(searchQuery: String): Intent {
        return Intent(context, ShareListActivity::class.java).apply {
            putExtra(SearchManager.QUERY, searchQuery)
        }
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
}
