package com.dinhlam.sharebox.ui.sharereceive

import android.content.Context
import android.content.pm.PackageManager
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.FirebaseStorageHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.pref.AppSharePref
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
    private val firebaseStorageHelper: FirebaseStorageHelper,
    private val boxRepository: BoxRepository,
    private val appSharePref: AppSharePref,
    private val videoHelper: VideoHelper,
) : BaseViewModel<ShareReceiveState>(ShareReceiveState()) {

    init {
        getLatestBox()
        getCurrentUserProfile()
        loadBoxes()
    }

    private fun getLatestBox() = execute {
        val boxId =
            appSharePref.getLatestActiveBoxId().takeIfNotNullOrBlank() ?: return@execute this
        val box = boxRepository.findOne(boxId) ?: return@execute this
        copy(currentBox = box)
    }

    fun getCurrentUserProfile() {
        setState { copy(showLoading = true) }
        doInBackground {
            val user = userRepository.findOne(userHelper.getCurrentUserId())
                ?: return@doInBackground setState { copy(showLoading = false) }
            setState { copy(activeUser = user, showLoading = false) }
        }
    }

    fun loadBoxes() = doInBackground {
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
                if (insertedShare.isVideoShare) {
                    handleShareVideo(insertedShare)
                }
                WorkerUtils.enqueueSyncShareToCloud(context, insertedShare.shareId)
                bookmarkCollection?.id?.let { pickedBookmarkCollectionId ->
                    bookmarkRepository.bookmark(
                        0, insertedShare.shareId, pickedBookmarkCollectionId
                    )
                    copy(isSaveSuccess = true, showLoading = false)
                } ?: copy(isSaveSuccess = true, showLoading = false)
            } ?: run {
                postShowToast(R.string.shares_error)
                copy(isSaveSuccess = false, showLoading = false)
            }
        }
    }

    private suspend fun handleShareVideo(share: Share) = withContext(Dispatchers.IO) {
        val shareUrl = share.shareData.cast<ShareData.ShareUrl>() ?: return@withContext
        videoHelper.syncVideo(share.shareId, shareUrl.url)
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
            val notificationManagerCompat = NotificationManagerCompat.from(context)

            val notificationBuilder = NotificationCompat.Builder(
                context, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID
            ).setContentText(context.getString(R.string.distribute_images_content))
                .setSubText(context.getString(R.string.distribute_images_title))
                .setAutoCancel(false).setProgress(100, 0, false)
                .setSmallIcon(R.drawable.ic_file_upload_white)

            val uploadId = 456789

            uris.forEachIndexed { index, uri ->
                val progress = index.plus(1f).div(uris.size).times(100).toInt()
                notificationBuilder.setProgress(100, progress, false).setContentTitle(
                        context.getString(
                            R.string.complete_progress, "${index.plus(1)}/${uris.size}"
                        )
                    )
                if (ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManagerCompat.notify(
                        uploadId, notificationBuilder.build()
                    )
                }
                firebaseStorageHelper.uploadShareImageFileWithoutNotification(
                    insertedShare.shareId, uri
                )
            }
            notificationManagerCompat.cancel(uploadId)
        }

        return share
    }

    fun setBox(box: BoxDetail?) = getState { state ->
        if (state.currentBox != box) {
            setState { copy(currentBox = box) }
            box?.let { nonNullBox ->
                doInBackground {
                    boxRepository.updateLastSeen(nonNullBox.boxId, nowUTCTimeInMillis())
                }
                appSharePref.setLatestActiveBoxId(nonNullBox.boxId)
            } ?: appSharePref.setLatestActiveBoxId("")
        }
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
        doInBackground {
            val boxDetail = boxRepository.findOne(boxId) ?: return@doInBackground setBox(null)
            setBox(boxDetail)
        }
    }
}
