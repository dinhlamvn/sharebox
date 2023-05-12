package com.dinhlam.sharebox.ui.sharereceive

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.loader.ImageLoader
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareMode
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.repository.ShareRepository
import com.dinhlam.sharebox.repository.UserRepository
import com.dinhlam.sharebox.utils.ShareUtils
import com.dinhlam.sharebox.utils.UserUtils
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ShareReceiveViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val gson: Gson,
    private val userSharePref: UserSharePref,
    private val userRepository: UserRepository,
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
        when (val shareData = state.shareData) {
            is ShareData.ShareUrl -> shareUrl(
                note,
                shareData.castNonNull(),
                state.shareMode,
                shareData.shareType
            )

            is ShareData.ShareText -> shareText(
                note,
                shareData.castNonNull(),
                state.shareMode,
                shareData.shareType
            )

            is ShareData.ShareImage -> shareImage(
                context,
                note,
                shareData.castNonNull(),
                state.shareMode,
                shareData.shareType
            )

            is ShareData.ShareImages -> shareImages(
                context,
                note,
                shareData.castNonNull(),
                state.shareMode,
                shareData.shareType
            )

            else -> return@execute
        }
    }

    private fun shareUrl(
        note: String?, shareWebLink: ShareData.ShareUrl, shareMode: ShareMode, shareType: String
    ) {
        val json = gson.toJson(shareWebLink)
        val share = Share(
            shareId = ShareUtils.createShareId(),
            shareType = shareType,
            shareData = json,
            shareNote = note,
            shareMode = shareMode.mode,
            shareUserId = UserUtils.fakeUserId
        )
        shareRepository.insert(share)
        setState { copy(isSaveSuccess = true) }
    }

    private fun shareText(
        note: String?, shareText: ShareData.ShareText, shareMode: ShareMode, shareType: String
    ) {
        val json = gson.toJson(shareText)
        val share = Share(
            shareId = ShareUtils.createShareId(),
            shareType = shareType,
            shareData = json,
            shareNote = note,
            shareMode = shareMode.mode,
            shareUserId = UserUtils.fakeUserId
        )
        shareRepository.insert(share)
        setState { copy(isSaveSuccess = true) }
    }

    private fun shareImage(
        context: Context,
        note: String?,
        shareImage: ShareData.ShareImage,
        shareMode: ShareMode, shareType: String
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
        val share = Share(
            shareId = ShareUtils.createShareId(),
            shareType = shareType,
            shareData = json,
            shareNote = note,
            shareMode = shareMode.mode,
            shareUserId = UserUtils.fakeUserId
        )
        shareRepository.insert(share)
        setState { copy(isSaveSuccess = true) }
    }

    private fun shareImages(
        context: Context,
        note: String?,
        shareImages: ShareData.ShareImages,
        shareMode: ShareMode, shareType: String
    ) {
        val uris = shareImages.uris.map { uri ->
            val bitmap = ImageLoader.get(context, uri)
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

        val saveShareImage = shareImages.copy(uris = uris)
        val json = gson.toJson(saveShareImage)
        val share = Share(
            shareId = ShareUtils.createShareId(),
            shareType = shareType,
            shareData = json,
            shareNote = note,
            shareMode = shareMode.mode,
            shareUserId = userSharePref.getActiveUserId()
        )
        shareRepository.insert(share)
        setState { copy(isSaveSuccess = true) }
    }

    fun setShareMode(shareMode: ShareMode) {
        userSharePref.setActiveShareMode(shareMode)
        setState { copy(shareMode = shareMode) }
    }
}
