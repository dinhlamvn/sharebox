package com.dinhlam.sharesaver.router

import android.content.Context
import android.content.Intent
import com.dinhlam.sharesaver.ui.home.HomeActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppRouter @Inject constructor(@ApplicationContext private val context: Context) {

    fun home(): Intent {
        return Intent(context, HomeActivity::class.java)
    }
}