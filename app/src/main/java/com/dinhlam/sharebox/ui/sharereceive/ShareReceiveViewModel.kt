package com.dinhlam.sharebox.ui.sharereceive

import android.content.Context
import android.webkit.MimeTypeMap
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.getExtensionFromUri
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareReceiveViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userHelper: UserHelper,
    private val userRepository: UserRepository,
    private val boxRepository: BoxRepository,
    private val videoHelper: VideoHelper,
) : BaseViewModel<ShareReceiveState>(ShareReceiveState()) {

    init {
        getBoxToArchiveContent()
    }

    private fun getBoxToArchiveContent() {
        suspend {
            boxRepository.findLastActiveBox()
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun getCurrentUserProfile() {
        suspend { userRepository.findOne(userHelper.getCurrentUserId()) }
            .execute { asyncLoad ->
                copy(activeUser = asyncLoad.data)
            }
    }

    fun setShareData(shareData: ShareData) = setState { copy(shareData = shareData) }

    fun share(note: String?, context: Context, box: BoxDetail) = getState { state ->
        suspend {
            val share = when (val shareData = state.shareData) {
                is ShareData.ShareUrl -> shareUrl(
                    note,
                    shareData,
                    box.boxId,
                )

                is ShareData.ShareText -> shareText(
                    note,
                    shareData,
                    box.boxId,
                )

                is ShareData.ShareImage -> shareImage(
                    context,
                    note,
                    shareData,
                    box.boxId,
                )

                is ShareData.ShareImages -> shareImages(
                    context,
                    note,
                    shareData,
                    box.boxId,
                )

                is ShareData.ShareFile -> shareFile(
                    context,
                    note,
                    shareData,
                    box.boxId,
                )

                else -> null
            }
            share != null
        }.execute { asyncLoad ->
            copy(asyncLoadArchive = asyncLoad)
        }
    }

    private suspend fun shareUrl(
        note: String?, shareData: ShareData.ShareUrl, boxId: String
    ): Share? {
        val isVideoShare = videoHelper.getVideoSource(shareData.url) != null
        return shareRepository.insert(
            shareData = shareData,
            shareNote = note,
            shareBoxId = boxId,
            isVideoShare = isVideoShare
        )
    }

    private suspend fun shareText(
        note: String?, shareData: ShareData.ShareText, boxId: String
    ): Share? {
        return shareRepository.insert(
            shareData = shareData,
            shareNote = note,
            shareBoxId = boxId
        )
    }

    private suspend fun shareImage(
        context: Context,
        note: String?,
        shareData: ShareData.ShareImage,
        boxId: String
    ): Share? = context.contentResolver.openInputStream(shareData.uri)?.use { inputStream ->
        val extension = context.getExtensionFromUri(shareData.uri)
            ?: return@use null
        val imageFile = FileUtils.createShareImageFile(context, extension) ?: return@use null
        imageFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        val newUri = FileUtils.getUriFromFile(context, imageFile)
        val saveShareImage = shareData.copy(uri = newUri)
        shareRepository.insert(
            shareData = saveShareImage,
            shareNote = note,
            shareBoxId = boxId,
        )
    }

    private suspend fun shareImages(
        context: Context, note: String?, shareData: ShareData.ShareImages, boxId: String
    ): Share? {
        val uris = shareData.uris.mapNotNull { uri ->
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(context.contentResolver.getType(uri))
                    ?: return@use null
                val imageFile =
                    FileUtils.createShareImageFile(context, extension) ?: return@use null
                imageFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                FileUtils.getUriFromFile(context, imageFile)
            }
        }

        val saveShareImages = shareData.copy(uris = uris)
        return shareRepository.insert(
            shareData = saveShareImages,
            shareNote = note,
            shareBoxId = boxId
        )
    }

    private suspend fun shareFile(
        context: Context, note: String?, shareData: ShareData.ShareFile, boxId: String
    ): Share? = context.contentResolver.openInputStream(shareData.uri)?.use { inputStream ->
        val extension = context.getExtensionFromUri(shareData.uri)
            ?: return@use null
        val file = FileUtils.createShareFile(context, extension) ?: return@use null
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        val newUri = FileUtils.getUriFromFile(context, file)
        val newShareFile = shareData.copy(uri = newUri)
        shareRepository.insert(
            shareData = newShareFile,
            shareNote = note,
            shareBoxId = boxId,
        )
    }

    fun setBox(boxId: String) {
        suspend { boxRepository.findOne(boxId) }
            .execute { asyncLoad ->
                copy(currentBox = asyncLoad.data)
            }
    }
}
