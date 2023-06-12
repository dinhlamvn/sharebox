package com.dinhlam.sharebox.ui.sharereceive

import android.content.Context
import android.webkit.MimeTypeMap
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.model.BoxDetail
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.helper.FirebaseStorageHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.utils.FileUtils
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
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val firebaseStorageHelper: FirebaseStorageHelper,
    private val boxRepository: BoxRepository,
) : BaseViewModel<ShareReceiveState>(ShareReceiveState()) {

    init {
        getCurrentUserProfile()
        loadBoxes()
    }

    fun getCurrentUserProfile() {
        setState { copy(showLoading = true) }
        backgroundTask {
            val user = userRepository.findOne(userHelper.getCurrentUserId())
                ?: return@backgroundTask setState { copy(showLoading = false) }
            setState { copy(activeUser = user, showLoading = false) }
        }
    }

    private fun loadBoxes() = backgroundTask {
        val boxes = boxRepository.findLatestBox()
        setState { copy(boxes = boxes) }
    }

    fun setShareData(shareData: ShareData) = setState { copy(shareData = shareData) }

    fun share(note: String?, context: Context) {
        setState { copy(showLoading = true) }
        execute(onError = {
            postShowToast(R.string.share_receive_error_share)
        }) {
            val share = when (val shareData = shareData) {
                is ShareData.ShareUrl -> shareUrl(
                    note,
                    shareData.castNonNull(),
                    currentBox,
                )

                is ShareData.ShareText -> shareText(
                    note,
                    shareData.castNonNull(),
                    currentBox,
                )

                is ShareData.ShareImage -> shareImage(
                    context,
                    note,
                    shareData.castNonNull(),
                    currentBox,
                )

                is ShareData.ShareImages -> shareImages(
                    context,
                    note,
                    shareData.castNonNull(),
                    currentBox,
                )

                else -> null
            }

            share?.let { insertedShare ->
                realtimeDatabaseRepository.push(insertedShare)
                bookmarkCollection?.id?.let { pickedBookmarkCollectionId ->
                    bookmarkRepository.bookmark(
                        0, insertedShare.shareId, pickedBookmarkCollectionId
                    )
                    copy(isSaveSuccess = true, showLoading = false)
                } ?: copy(isSaveSuccess = true, showLoading = false)
            } ?: run {
                postShowToast(R.string.snap_error)
                copy(isSaveSuccess = false, showLoading = false)
            }
        }
    }

    private suspend fun shareUrl(
        note: String?, shareData: ShareData.ShareUrl, shareBox: BoxDetail?
    ): Share? {
        return shareRepository.insert(
            shareData = shareData,
            shareNote = note,
            shareBoxId = shareBox?.boxId,
            shareUserId = userHelper.getCurrentUserId()
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
        val share = shareRepository.insert(
            shareData = saveShareImage,
            shareNote = note,
            shareBoxId = shareBox?.boxId,
            shareUserId = userHelper.getCurrentUserId()
        )

        share?.let { insertedShare ->
            firebaseStorageHelper.uploadShareImageFile(context, insertedShare.shareId, newUri)
        }

        share
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
        val share = shareRepository.insert(
            shareData = saveShareImages,
            shareNote = note,
            shareBoxId = shareBox?.boxId,
            shareUserId = userHelper.getCurrentUserId()
        )

        share?.let { insertedShare ->
            uris.forEach { uri ->
                firebaseStorageHelper.uploadShareImageFile(context, insertedShare.shareId, uri)
            }
        }

        return share
    }

    fun setBox(box: BoxDetail?) {
        setState { copy(currentBox = box) }
        box?.let { nonNullBox ->
            backgroundTask {
                boxRepository.updateLastSeen(nonNullBox.boxId, nowUTCTimeInMillis())
            }
        }
    }

    fun setBookmarkCollection(pickedId: String?) {
        pickedId?.let { collectionId ->
            backgroundTask {
                val bookmarkCollection = bookmarkCollectionRepository.find(collectionId)
                setState { copy(bookmarkCollection = bookmarkCollection) }
            }
        } ?: setState { copy(bookmarkCollection = null) }
    }
}
