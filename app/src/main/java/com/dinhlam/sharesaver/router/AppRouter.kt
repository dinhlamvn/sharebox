package com.dinhlam.sharesaver.router

import android.content.Context
import android.content.Intent
import com.dinhlam.sharesaver.ui.home.HomeActivity
import com.dinhlam.sharesaver.ui.list.ShareListActivity
import com.dinhlam.sharesaver.ui.setting.SettingActivity
import com.dinhlam.sharesaver.utils.ExtraUtils
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
}