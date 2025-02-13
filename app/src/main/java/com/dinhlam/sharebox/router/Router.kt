package com.dinhlam.sharebox.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.dinhlam.sharebox.model.BookmarkCollectionDetail

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
    fun boxForm(context: Context, boxId: String?): Intent
    fun setting(): Intent

    fun settingCompose(): Intent

    fun boxDetail(context: Context, boxId: String, isFromInvited: Boolean): Intent

    fun profile(context: Context): Intent
    fun textInput(context: Context, title: String?, text: String?, isEdit: Boolean): Intent

    fun shareLink(context: Context, uri: Uri?): Intent
    fun downloadBottomSheet(context: Context, urls: List<String>): Intent

    fun bookmark(context: Context): Intent

    fun imageViewer(context: Context, uris: List<Uri>): Intent
    fun boxList(context: Context, title: String?): Intent

    fun trash(context: Context): Intent
    fun boxMembers(context: Context, boxId: String): Intent
    fun boxInvited(context: Context): Intent

    fun clipboard(context: Context): Intent

    fun pickFile(context: Context): Intent

    fun shareToOtherIntent(context: Context, uri: Uri?, mimeType: String?): Intent?

    fun guideline(context: Context): Intent
}