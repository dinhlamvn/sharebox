package com.dinhlam.sharebox.ui.sharereceive

import android.content.Context
import android.webkit.MimeTypeMap
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.model.Box
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShareReceiveViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userSharePref: UserSharePref,
    private val userRepository: UserRepository,
    private val bookmarkCollectionRepository: BookmarkCollectionRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
) : BaseViewModel<ShareReceiveState>(ShareReceiveState()) {

    init {
        getActiveUserInfo()
    }

    private fun getActiveUserInfo() = backgroundTask {
        val activeUserId =
            userSharePref.getActiveUserId().takeIfNotNullOrBlank() ?: return@backgroundTask
        val user = userRepository.findOne(activeUserId) ?: return@backgroundTask
        setState { copy(activeUser = user) }
    }

    fun setShareData(shareData: ShareData) = setState { copy(shareData = shareData) }

    fun share(note: String?, context: Context) = execute(onError = {
        postShowToast(R.string.share_receive_error_share)
    }) { state ->
        setState { copy(showLoading = true) }

        val share = when (val shareData = state.shareData) {
            is ShareData.ShareUrl -> shareUrl(
                note,
                shareData.castNonNull(),
                state.shareBox,
            )

            is ShareData.ShareText -> shareText(
                note,
                shareData.castNonNull(),
                state.shareBox,
            )

            is ShareData.ShareImage -> shareImage(
                context,
                note,
                shareData.castNonNull(),
                state.shareBox,
            )

            is ShareData.ShareImages -> shareImages(
                context,
                note,
                shareData.castNonNull(),
                state.shareBox,
            )

            else -> null
        }

        share?.let { insertedShare ->
            if (state.shareBox !is Box.PersonalBox) {
                realtimeDatabaseRepository.push(insertedShare)
            }
            state.bookmarkCollection?.id?.let { pickedBookmarkCollectionId ->
                bookmarkRepository.bookmark(0, insertedShare.shareId, pickedBookmarkCollectionId)
                setState { copy(isSaveSuccess = true, showLoading = false) }
            } ?: setState { copy(isSaveSuccess = true, showLoading = false) }
        } ?: run {
            postShowToast(R.string.snap_error)
            setState { copy(isSaveSuccess = false, showLoading = false) }
        }
    }

    private suspend fun shareUrl(
        note: String?, shareData: ShareData.ShareUrl, shareBox: Box
    ): Share? {
        return shareRepository.insert(
            shareData = shareData,
            shareNote = note,
            shareBox = shareBox,
            shareUserId = userSharePref.getActiveUserId()
        )
    }

    private suspend fun shareText(
        note: String?, shareData: ShareData.ShareText, shareBox: Box
    ): Share? {
        return shareRepository.insert(
            shareData = shareData,
            shareNote = note,
            shareBox = shareBox,
            shareUserId = userSharePref.getActiveUserId()
        )
    }

    private suspend fun shareImage(
        context: Context, note: String?, shareData: ShareData.ShareImage, shareBox: Box
    ): Share? = context.contentResolver.openInputStream(shareData.uri)?.use { inputStream ->
        val imageFileDir = context.getExternalFilesDir("share_images") ?: return@use null
        if (!imageFileDir.exists() && !imageFileDir.mkdir()) {
            return@use null
        }
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
            shareBox = shareBox,
            shareUserId = userSharePref.getActiveUserId()
        )
    }

    private suspend fun shareImages(
        context: Context, note: String?, shareData: ShareData.ShareImages, shareBox: Box
    ): Share? {
        val imageFileDir = context.getExternalFilesDir("share_images") ?: return null
        if (!imageFileDir.exists() && !imageFileDir.mkdir()) {
            return null
        }
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
            shareBox = shareBox,
            shareUserId = userSharePref.getActiveUserId()
        )
    }

    fun setShareBox(shareBox: Box) {
        setState { copy(shareBox = shareBox) }
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
