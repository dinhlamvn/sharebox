package com.dinhlam.sharebox.ui.sharereceive

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.utils.ShareUtils
import com.dinhlam.sharebox.utils.UserUtils
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
) : BaseViewModel<ShareReceiveState>(ShareReceiveState(shareMode = userSharePref.getActiveShareMode())) {

    init {
        getActiveUserInfo()
    }

    private fun getActiveUserInfo() = backgroundTask {
        val activeUserId =
            userSharePref.getActiveUserId().takeIfNotNullOrBlank() ?: return@backgroundTask
        val user = userRepository.findOne(activeUserId) ?: return@backgroundTask
        setState { copy(activeUser = user) }
    }

    fun setShareInfo(shareInfo: ShareData) =
        setState { copy(shareData = shareInfo) }

    fun share(note: String?, context: Context) = execute(onError = {
        postShowToast(R.string.share_receive_error_share)
    }) { state ->
        setState { copy(showLoading = true) }

        val shareId = when (val shareData = state.shareData) {
            is ShareData.ShareUrl -> shareUrl(
                note,
                shareData.castNonNull(),
                state.shareMode,
            )

            is ShareData.ShareText -> shareText(
                note,
                shareData.castNonNull(),
                state.shareMode,
            )

            is ShareData.ShareImage -> shareImage(
                context,
                note,
                shareData.castNonNull(),
                state.shareMode,
            )

            is ShareData.ShareImages -> shareImages(
                context,
                note,
                shareData.castNonNull(),
                state.shareMode,
            )

            else -> ""
        }

        shareId.takeIfNotNullOrBlank()?.let { shareIdInserted ->
            state.bookmarkCollection?.id?.let { pickedBookmarkCollectionId ->
                bookmarkRepository.bookmark(0, shareIdInserted, pickedBookmarkCollectionId)
                setState { copy(isSaveSuccess = true, showLoading = false) }
            }
        } ?: setState { copy(isSaveSuccess = false, showLoading = false) }
    }

    private suspend fun shareUrl(
        note: String?, shareData: ShareData.ShareUrl, shareMode: ShareMode
    ): String {
        val share = Share(
            shareId = ShareUtils.createShareId(),
            shareData = shareData,
            shareNote = note,
            shareMode = shareMode,
            shareUserId = UserUtils.fakeUserId
        )
        shareRepository.insert(share)
        return share.shareId
    }

    private suspend fun shareText(
        note: String?, shareData: ShareData.ShareText, shareMode: ShareMode
    ): String {
        val share = Share(
            shareId = ShareUtils.createShareId(),
            shareData = shareData,
            shareNote = note,
            shareMode = shareMode,
            shareUserId = UserUtils.fakeUserId
        )
        shareRepository.insert(share)
        return share.shareId
    }

    private suspend fun shareImage(
        context: Context,
        note: String?,
        shareData: ShareData.ShareImage,
        shareMode: ShareMode
    ): String {
        val bitmap = ImageLoader.instance.get(context, shareData.uri) ?: return ""
        val imagePath = context.getExternalFilesDir("share_images")!!
        if (!imagePath.exists()) {
            imagePath.mkdir()
        }
        val imageFile = File(imagePath, "share_image_${System.currentTimeMillis()}.jpg")

        withContext(Dispatchers.IO) {
            imageFile.createNewFile()
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageFile.outputStream())
        val newUri = FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.file_provider", imageFile
        )
        val saveShareImage = shareData.copy(uri = newUri)
        val share = Share(
            shareId = ShareUtils.createShareId(),
            shareData = saveShareImage,
            shareNote = note,
            shareMode = shareMode,
            shareUserId = UserUtils.fakeUserId
        )
        shareRepository.insert(share)
        return share.shareId
    }

    private suspend fun shareImages(
        context: Context,
        note: String?,
        shareData: ShareData.ShareImages,
        shareMode: ShareMode
    ): String {
        val uris = shareData.uris.mapNotNull { uri ->
            val bitmap = ImageLoader.instance.get(context, uri) ?: return@mapNotNull null
            val imagePath = context.getExternalFilesDir("share_images")!!
            if (!imagePath.exists()) {
                imagePath.mkdir()
            }
            val imageFile = File(imagePath, "share_image_${System.currentTimeMillis()}.jpg")
            imageFile.createNewFile()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageFile.outputStream())
            FileProvider.getUriForFile(
                context, "${BuildConfig.APPLICATION_ID}.file_provider", imageFile
            )
        }

        val saveShareImages = shareData.copy(uris = uris)
        val share = Share(
            shareId = ShareUtils.createShareId(),
            shareData = saveShareImages,
            shareNote = note,
            shareMode = shareMode,
            shareUserId = userSharePref.getActiveUserId()
        )
        shareRepository.insert(share)
        return share.shareId
    }

    fun setShareMode(shareMode: ShareMode) {
        userSharePref.setActiveShareMode(shareMode)
        setState { copy(shareMode = shareMode) }
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
