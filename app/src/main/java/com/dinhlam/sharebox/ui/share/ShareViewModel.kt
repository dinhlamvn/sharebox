package com.dinhlam.sharebox.ui.share

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.loader.ImageLoader
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.repository.FolderRepository
import com.dinhlam.sharebox.repository.ShareRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val shareRepository: ShareRepository,
    private val gson: Gson,
    private val appSharePref: AppSharePref
) : BaseViewModel<ShareState>(ShareState()) {

    fun setShareInfo(shareInfo: ShareState.ShareInfo) = executeJob {
        val folders = folderRepository.getAll(SortType.NONE)
        val historySelectedFolder = appSharePref.getLastSelectedFolder()
        val folderId = when {
            historySelectedFolder.isNotBlank() -> historySelectedFolder
            shareInfo is ShareState.ShareInfo.ShareText -> "folder_text"
            shareInfo is ShareState.ShareInfo.ShareWebLink -> "folder_web"
            shareInfo is ShareState.ShareInfo.ShareImage -> "folder_image"
            else -> "folder_home"
        }
        setState { copy(folders = folders, shareInfo = shareInfo) }
        setSelectedFolder(folderId)
    }

    fun setSelectedFolder(folderId: String) = withState { data ->
        val folder = data.folders.firstOrNull { it.id == folderId }
            ?: data.folders.sortedByDescending { it.createdAt }.getOrNull(0)
        setState { copy(selectedFolder = folder) }
    }

    fun saveShare(note: String, context: Context) = execute { data ->
        val folderId: String = data.selectedFolder?.id ?: return@execute
        if (data.shareInfo is ShareState.ShareInfo.ShareWebLink) {
            return@execute saveWebLink(folderId, note, data.shareInfo)
        }
        if (data.shareInfo is ShareState.ShareInfo.ShareText) {
            return@execute saveShareText(folderId, note, data.shareInfo)
        }
        if (data.shareInfo is ShareState.ShareInfo.ShareImage) {
            return@execute saveShareImage(context, folderId, note, data.shareInfo)
        }
    }

    private fun saveWebLink(
        folderId: String,
        note: String,
        shareWebLink: ShareState.ShareInfo.ShareWebLink
    ) {
        val json = gson.toJson(shareWebLink)
        val share = Share(
            folderId = folderId,
            shareType = shareWebLink.shareType,
            shareInfo = json,
            shareNote = note
        )
        shareRepository.insert(share)
        setState { copy(isSaveSuccess = true) }
    }

    private fun saveShareText(
        folderId: String,
        note: String,
        shareText: ShareState.ShareInfo.ShareText
    ) {
        val json = gson.toJson(shareText)
        val share = Share(
            folderId = folderId,
            shareType = shareText.shareType,
            shareInfo = json,
            shareNote = note
        )
        shareRepository.insert(share)
        setState { copy(isSaveSuccess = true) }
    }

    private fun saveShareImage(
        context: Context,
        folderId: String,
        note: String,
        shareImage: ShareState.ShareInfo.ShareImage
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
            context,
            "${BuildConfig.APPLICATION_ID}.file_provider",
            imageFile
        )
        val saveShareImage = shareImage.copy(uri = newUri)
        val json = gson.toJson(saveShareImage)
        val share =
            Share(folderId = folderId, shareType = "image", shareInfo = json, shareNote = note)
        shareRepository.insert(share)
        setState { copy(isSaveSuccess = true) }
    }

    fun setSelectedFolderAfterCreate(folderId: String) = executeJob {
        val folders = folderRepository.getAll(SortType.NONE)
        val folder = folders.firstOrNull { it.id == folderId }
        setState { copy(folders = folders, selectedFolder = folder) }
    }

    fun saveLastSelectedFolder(folderId: String) = appSharePref.setLastSelectedFolder(folderId)
}
