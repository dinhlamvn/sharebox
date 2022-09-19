package com.dinhlam.sharesaver.ui.share

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.dinhlam.sharesaver.BuildConfig
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.AppDatabase
import com.dinhlam.sharesaver.database.entity.Share
import com.dinhlam.sharesaver.extensions.isWebLink
import com.dinhlam.sharesaver.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharesaver.loader.ImageLoader
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val appDatabase: AppDatabase, private val gson: Gson
) : BaseViewModel<ShareData>(ShareData()) {

    fun setShareInfo(shareInfo: ShareData.ShareInfo) = setData {
        copy(shareInfo = shareInfo)
    }

    fun saveShare(note: String, context: Context) = executeWithData { myData ->
        if (myData.shareInfo is ShareData.ShareInfo.ShareText) {
            return@executeWithData saveShareText(note, myData.shareInfo)
        }
        if (myData.shareInfo is ShareData.ShareInfo.ShareImage) {
            return@executeWithData saveShareImage(context, note, myData.shareInfo)
        }
    }

    private fun saveShareText(note: String, shareText: ShareData.ShareInfo.ShareText) {
        val nonNullText = shareText.text.takeIfNotNullOrBlank() ?: return
        val json = gson.toJson(shareText)
        val share = if (nonNullText.isWebLink()) {
            Share(shareType = "web-link", shareInfo = json, shareNote = note)
        } else {
            Share(shareType = "text", shareInfo = json, shareNote = note)
        }
        appDatabase.shareDao().insertAll(share)
        setData { copy(isSaveSuccess = true) }
    }

    private fun saveShareImage(
        context: Context, note: String, shareImage: ShareData.ShareInfo.ShareImage
    ) {
        val bitmap = ImageLoader.get(context, shareImage.uri)
        val imagePath = context.getExternalFilesDir("share_images")!!
        if (!imagePath.exists()) {
            imagePath.mkdir()
        }
        val imageFile = File(imagePath, "share_image_${System.currentTimeMillis()}.jpg")
        imageFile.createNewFile()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageFile.outputStream())
        val newUri = FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.file_provider", imageFile
        )
        val saveShareImage = shareImage.copy(uri = newUri)
        val json = gson.toJson(saveShareImage)
        val share = Share(shareType = "image", shareInfo = json, shareNote = note)
        appDatabase.shareDao().insertAll(share)
        setData { copy(isSaveSuccess = true) }
    }
}