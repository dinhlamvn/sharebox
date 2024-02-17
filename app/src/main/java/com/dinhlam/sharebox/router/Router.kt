package com.dinhlam.sharebox.router

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.model.DownloadData

interface Router {
    fun home(isNewTask: Boolean = false): Intent
    fun signIn(signInForResult: Boolean = false): Intent
    fun moveToChromeCustomTab(
        context: Context,
        url: String,
        boxId: String?,
        boxName: String?,
        supportDownload: Boolean
    )
    fun moveToBrowser(url: String)
    fun bookmarkCollectionFormIntent(context: Context): Intent
    fun bookmarkCollectionFormIntent(
        context: Context,
        bookmarkCollection: BookmarkCollectionDetail
    ): Intent

    fun bookmarkListItemIntent(context: Context, bookmarkCollectionId: String): Intent
    fun pickImageIntent(isMultiple: Boolean = false): Intent
    fun passcodeIntent(context: Context, desc: String? = null): Intent
    fun passcodeIntent(context: Context, passcode: String, desc: String? = null): Intent

    fun passcodeIntent(
        context: Context,
        passcode: String,
        extras: Bundle,
        desc: String? = null
    ): Intent

    fun viewIntent(url: String): Intent
    fun playStoreIntent(packageName: String): Intent
    fun boxIntent(context: Context): Intent
    fun settingIntent(): Intent

    fun boxDetail(context: Context, boxId: String): Intent

    fun profile(context: Context): Intent
    fun shareText(context: Context): Intent

    fun shareLink(context: Context): Intent
    fun downloadPopup(context: Context, videos: List<DownloadData>, audios: List<DownloadData>, images: List<DownloadData>): Intent

    fun bookmark(context: Context): Intent
}