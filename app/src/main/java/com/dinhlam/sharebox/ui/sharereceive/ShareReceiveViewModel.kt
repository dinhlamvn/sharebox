package com.dinhlam.sharebox.ui.sharereceive

import android.content.Context
import android.webkit.MimeTypeMap
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.utils.FileUtils
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShareReceiveViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userHelper: UserHelper,
    private val userRepository: UserRepository,
    private val bookmarkCollectionRepository: BookmarkCollectionRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val boxRepository: BoxRepository,
    private val videoHelper: VideoHelper,
) : BaseViewModel<ShareReceiveState>(ShareReceiveState()) {

    fun getCurrentUserProfile() {
        suspend { userRepository.findOne(userHelper.getCurrentUserId()) }
            .execute { asyncLoad ->
                copy(activeUser = asyncLoad.data)
            }
    }

    fun setShareData(shareData: ShareData) = setState { copy(shareData = shareData) }

    fun share(note: String?, context: Context) = getState { state ->
        suspend {
            val share = when (val shareData = state.shareData) {
                is ShareData.ShareUrl -> shareUrl(
                    note,
                    shareData.castNonNull(),
                    state.currentBox,
                )

                is ShareData.ShareText -> shareText(
                    note,
                    shareData.castNonNull(),
                    state.currentBox,
                )

                is ShareData.ShareImage -> shareImage(
                    context,
                    note,
                    shareData.castNonNull(),
                    state.currentBox,
                )

                is ShareData.ShareImages -> shareImages(
                    context,
                    note,
                    shareData.castNonNull(),
                    state.currentBox,
                )

                else -> null
            }
            share?.let { insertedShare ->
                WorkerUtils.enqueueSyncShareToCloud(context, insertedShare.shareId)
                state.bookmarkCollection?.id?.let { pickedBookmarkCollectionId ->
                    bookmarkRepository.bookmark(
                        0, insertedShare.shareId, pickedBookmarkCollectionId
                    )
                }
                true
            } ?: false
        }.execute { asyncLoad ->
            copy(asyncLoadArchive = asyncLoad)
        }
    }

    private suspend fun shareUrl(
        note: String?, shareData: ShareData.ShareUrl, shareBox: BoxDetail?
    ): Share? {
        val isVideoShare = videoHelper.getVideoSource(shareData.url) != null
        return shareRepository.insert(
            shareData = shareData,
            shareNote = note,
            shareBoxId = shareBox?.boxId,
            shareUserId = userHelper.getCurrentUserId(),
            isVideoShare = isVideoShare
        )
    }

    private suspend fun shareText(
        note: String?, shareData: ShareData.ShareText, shareBox: BoxDetail?
    ): Share? {
        return shareRepository.insert(
            shareData = shareData,
            shareNote = note,
            shareBoxId = shareBox?.boxId,
            shareUserId = userHelper.getCurrentUserId()
        )
    }

    private suspend fun shareImage(
        context: Context, note: String?, shareData: ShareData.ShareImage, shareBox: BoxDetail?
    ): Share? = context.contentResolver.openInputStream(shareData.uri)?.use { inputStream ->
        val imageFileDir = FileUtils.createShareImagesDir(context) ?: return@use null
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(context.contentResolver.getType(shareData.uri))
            ?: return@use null
        val imageFile = File(imageFileDir, FileUtils.randomImageFileName(extension))

        withContext(Dispatchers.IO) {
            imageFile.createNewFile()
        }

        imageFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        val newUri = FileUtils.getUriFromFile(context, imageFile)
        val saveShareImage = shareData.copy(uri = newUri)
        shareRepository.insert(
            shareData = saveShareImage,
            shareNote = note,
            shareBoxId = shareBox?.boxId,
            shareUserId = userHelper.getCurrentUserId()
        )
    }

    private suspend fun shareImages(
        context: Context, note: String?, shareData: ShareData.ShareImages, shareBox: BoxDetail?
    ): Share? {
        val imageFileDir = FileUtils.createShareImagesDir(context) ?: return null
        val uris = shareData.uris.mapNotNull { uri ->
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(context.contentResolver.getType(uri))
                    ?: return@use null

                val imageFile = File(imageFileDir, FileUtils.randomImageFileName(extension))
                imageFile.createNewFile()

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
            shareBoxId = shareBox?.boxId,
            shareUserId = userHelper.getCurrentUserId()
        )
    }

    fun setBookmarkCollection(pickedId: String?) {
        pickedId?.let { collectionId ->
            doInBackground {
                val bookmarkCollection = bookmarkCollectionRepository.find(collectionId)
                setState { copy(bookmarkCollection = bookmarkCollection) }
            }
        } ?: setState { copy(bookmarkCollection = null) }
    }

    fun setBox(boxId: String) {
        suspend { boxRepository.findOne(boxId) }
            .execute { asyncLoad ->
                copy(currentBox = asyncLoad.data)
            }
    }
}
