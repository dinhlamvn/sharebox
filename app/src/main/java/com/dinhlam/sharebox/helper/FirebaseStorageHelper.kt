package com.dinhlam.sharebox.helper

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.utils.FileUtils
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageHelper @Inject constructor(
    private val storage: FirebaseStorage
) {

    private val shareImagesRef by lazy { storage.getReference("shareImages") }

    private val avatarImagesRef by lazy { storage.getReference("avatarImages") }

    suspend fun uploadUserAvatar(userId: String, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            val ref = avatarImagesRef.child(getUploadAvatarFilePath(userId))
            val uploadTask =
                ref.putFile(uri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { error ->
                        throw error
                    }
                }

                ref.downloadUrl
            }.await()?.toString()
        }

    suspend fun uploadShareImageFile(
        context: Context, shareId: String, uri: Uri
    ): UploadTask.TaskSnapshot = withContext(Dispatchers.IO) {

        val notificationManagerCompat = NotificationManagerCompat.from(context)

        val notificationBuilder = NotificationCompat.Builder(
            context, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID
        ).setContentText("We are distributing image to other people").setSubText("Distribute image")
            .setProgress(100, 0, false).setSmallIcon(R.drawable.ic_file_upload_white)

        val uploadId = 456789
        shareImagesRef.child(getUploadFilePath(shareId, uri)).putFile(uri)
            .addOnProgressListener { taskSnapshot ->
                val progress =
                    ((100 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                notificationBuilder.setProgress(100, progress, false)
                if (ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManagerCompat.notify(
                        uploadId, notificationBuilder.build()
                    )
                }
            }.addOnCompleteListener {
                notificationManagerCompat.cancel(uploadId)
            }.addOnFailureListener { error ->
                Logger.error(error)
            }.addOnSuccessListener {
                Logger.debug("Success upload file $uri")
            }.await()
    }

    suspend fun downloadImageFile(
        context: Context, shareId: String, uri: Uri
    ): FileDownloadTask.TaskSnapshot? {
        return withContext(Dispatchers.IO) {
            val imageFileDir = FileUtils.createShareImagesDir(context) ?: return@withContext null

            val notificationManagerCompat = NotificationManagerCompat.from(context)

            val notificationBuilder = NotificationCompat.Builder(
                context, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID
            ).setContentText("We are downloading image file which share from other user")
                .setSubText("Download image").setProgress(100, 0, false)
                .setSmallIcon(R.drawable.ic_file_download_white)

            val imageFile = File(imageFileDir, FileUtils.getFileNameFromUri(uri))
            imageFile.createNewFile()

            val downloadId = 123123
            shareImagesRef.child(getUploadFilePath(shareId, uri)).getFile(imageFile)
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        ((100 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                    notificationBuilder.setProgress(100, progress, false)
                    if (ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationManagerCompat.notify(
                            downloadId, notificationBuilder.build()
                        )
                    }
                }.addOnCompleteListener {
                    notificationManagerCompat.cancel(downloadId)
                }.addOnSuccessListener {
                    Logger.debug("Success download file $uri")
                }.addOnFailureListener { error ->
                    Logger.error(error)
                }.await()
        }
    }

    suspend fun getImageDownloadUri(
        shareId: String, uri: Uri
    ): Uri {
        return withContext(Dispatchers.IO) {
            shareImagesRef.child(getUploadFilePath(shareId, uri)).downloadUrl.await()
        }
    }

    private fun getUploadFilePath(shareId: String, uri: Uri): String {
        val uploadFileName = FileUtils.getFileNameFromUri(uri)
        return "$shareId/$uploadFileName"
    }

    private fun getUploadAvatarFilePath(userId: String): String {
        return "avatar_$userId"
    }
}