package com.dinhlam.sharesaver.ui.share

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.dinhlam.sharesaver.BuildConfig
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Share
import com.dinhlam.sharesaver.loader.ImageLoader
import com.dinhlam.sharesaver.repository.FolderRepository
import com.dinhlam.sharesaver.repository.ShareRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val shareRepository: ShareRepository,
    private val gson: Gson
) : BaseViewModel<ShareData>(ShareData()) {

    init {
        execute {
            folderRepository.getAll()
        }
    }

    fun setShareInfo(shareInfo: ShareData.ShareInfo) = execute {
        val folderId = when (shareInfo) {
            is ShareData.ShareInfo.ShareText -> "folder_text"
            is ShareData.ShareInfo.ShareWebLink -> "folder_web"
            is ShareData.ShareInfo.ShareImage -> "folder_image"
            else -> "folder_home"
        }
        val folder = folderRepository.get(folderId)
        setData {
            copy(shareInfo = shareInfo, selectedFolder = folder)
        }
    }

    fun saveShare(note: String, context: Context) = executeWithData { data ->
        val folderId: String = data.selectedFolder?.id ?: return@executeWithData
        if (data.shareInfo is ShareData.ShareInfo.ShareWebLink) {
            return@executeWithData saveWebLink(folderId, note, data.shareInfo)
        }
        if (data.shareInfo is ShareData.ShareInfo.ShareText) {
            return@executeWithData saveShareText(folderId, note, data.shareInfo)
        }
        if (data.shareInfo is ShareData.ShareInfo.ShareImage) {
            return@executeWithData saveShareImage(context, folderId, note, data.shareInfo)
        }
    }

    private fun saveWebLink(
        folderId: String, note: String, shareWebLink: ShareData.ShareInfo.ShareWebLink
    ) {
        val json = gson.toJson(shareWebLink)
        val share = Share(
            folderId = folderId,
            shareType = shareWebLink.shareType,
            shareInfo = json,
            shareNote = note
        )
        shareRepository.insert(share)
        setData { copy(isSaveSuccess = true) }
    }

    private fun saveShareText(
        folderId: String, note: String, shareText: ShareData.ShareInfo.ShareText
    ) {
        val json = gson.toJson(shareText)
        val share = Share(
            folderId = folderId, shareType = shareText.shareType, shareInfo = json, shareNote = note
        )
        shareRepository.insert(share)
        setData { copy(isSaveSuccess = true) }
    }

    private fun saveShareImage(
        context: Context, folderId: String, note: String, shareImage: ShareData.ShareInfo.ShareImage
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
        val share =
            Share(folderId = folderId, shareType = "image", shareInfo = json, shareNote = note)
        shareRepository.insert(share)
        setData { copy(isSaveSuccess = true) }
    }
}