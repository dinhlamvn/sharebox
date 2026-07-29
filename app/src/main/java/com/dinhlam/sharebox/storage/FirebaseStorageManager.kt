package com.dinhlam.sharebox.storage

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FirebaseStorageManager @Inject constructor(
    private val storage: FirebaseStorage,
    private val userHelper: UserHelper,
) {
    private val fileRef by lazy { storage.getReference("files") }

    private val avatarImagesRef by lazy { storage.getReference("avatarImages") }

    private val boxExportsRef by lazy { storage.getReference("box-exports") }

    suspend fun uploadUserAvatar(uri: Uri): String? =
        withContext(Dispatchers.IO) {
            val ref = avatarImagesRef.child(getUploadAvatarFilePath(userHelper.getCurrentUserId()))
            ref.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { error ->
                        throw error
                    }
                }
                ref.downloadUrl
            }.await()?.toString()
        }

    suspend fun uploadFile(
        context: Context, shareId: String, uri: Uri, fileNumber: Int = 0
    ): Uri? = withContext(Dispatchers.IO) {
        val notificationManagerCompat = NotificationManagerCompat.from(context)

        val notificationBuilder = NotificationCompat.Builder(
            context, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID
        ).setContentText(context.getString(R.string.distribute_file_content))
            .setSubText(context.getString(R.string.distribute_file_title))
            .setProgress(100, 0, false).setSmallIcon(R.drawable.ic_file_upload_white)

        val notificationId = getNotificationId()
        val task = fileRef.child(getUploadFilePath(shareId, fileNumber)).putFile(uri)
            .addOnProgressListener { taskSnapshot ->
                val progress =
                    ((100 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                notificationBuilder.setProgress(100, progress, false)
                if (ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManagerCompat.notify(
                        notificationId, notificationBuilder.build()
                    )
                }
            }.addOnCompleteListener {
                notificationManagerCompat.cancel(notificationId)
            }.addOnFailureListener { error ->
                Logger.error(error)
            }.addOnSuccessListener {
                Logger.debug("Success upload file $uri")
            }.await()

        if (task.task.isSuccessful) {
            getFileDownloadUri(shareId, fileNumber)
        } else {
            Logger.error("Upload file to firebase failed: Uri $uri")
            null
        }
    }

    suspend fun uploadFileWithoutNotification(
        shareId: String,
        uri: Uri,
        fileNumber: Int = 0
    ): Uri? = withContext(Dispatchers.IO) {
        val currentUri = getFileDownloadUri(shareId, fileNumber)
        if (currentUri != null) {
            return@withContext currentUri
        }
        val task = fileRef.child(getUploadFilePath(shareId, fileNumber)).putFile(uri).await()
        if (task.task.isSuccessful) {
            getFileDownloadUri(shareId, fileNumber)
        } else {
            Logger.error("Upload file to firebase failed: Uri $uri")
            null
        }
    }

    suspend fun uploadBoxAsset(
        boxId: String,
        shareId: String,
        uri: Uri,
        fileNumber: Int = 0,
    ): Uri = withContext(Dispatchers.IO) {
        val ref = boxExportsRef.child(boxAssetPath(boxId, shareId, fileNumber))
        ref.putFile(uri).await()
        ref.downloadUrl.await()
    }

    suspend fun uploadBoxManifest(boxId: String, json: String) =
        withContext(Dispatchers.IO) {
            val metadata = StorageMetadata.Builder()
                .setContentType("application/json")
                .setCustomMetadata("schemaVersion", "1")
                .build()
            boxExportsRef.child(manifestPath(boxId))
                .putBytes(json.toByteArray(Charsets.UTF_8), metadata)
                .await()
        }

    suspend fun downloadBoxManifest(boxId: String): String = withContext(Dispatchers.IO) {
        val bytes = boxExportsRef.child(manifestPath(boxId))
            .getBytes(MAX_MANIFEST_BYTES)
            .await()
        bytes.toString(Charsets.UTF_8)
    }

    suspend fun downloadBoxAsset(uri: Uri, destination: File): File =
        withContext(Dispatchers.IO) {
            destination.parentFile?.mkdirs()
            storage.getReferenceFromUrl(uri.toString()).getFile(destination).await()
            destination
        }

    suspend fun downloadFile(
        context: Context, shareId: String, uri: Uri, destUri: Uri, fileNumber: Int = 0
    ): FileDownloadTask.TaskSnapshot = withContext(Dispatchers.IO) {
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        val notificationBuilder = NotificationCompat.Builder(
            context, AppConsts.NOTIFICATION_DOWNLOAD_CHANNEL_ID
        ).setContentText("We are downloading the file.")
            .setSubText("Downloading...").setProgress(100, 0, false)
            .setSmallIcon(R.drawable.ic_file_download_white)

        val downloadId = getNotificationId()
        fileRef.child(getUploadFilePath(shareId, fileNumber)).getFile(destUri)
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

    private suspend fun getFileDownloadUri(
        shareId: String, fileNumber: Int = 0
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            fileRef.child(getUploadFilePath(shareId, fileNumber)).downloadUrl.await()
        } catch (e: Exception) {
            Logger.error("Get file url failed: ${e.message}")
            null
        }
    }

    private fun getUploadFilePath(shareId: String, fileNumber: Int = 0): String {
        val uploadFileName = "File_${shareId}_$fileNumber"
        return "${userHelper.getCurrentUserId()}/$shareId/$uploadFileName"
    }

    private fun getUploadAvatarFilePath(userId: String): String {
        return "avatar_$userId"
    }

    private fun manifestPath(boxId: String) = "$boxId/manifest.json"

    private fun boxAssetPath(boxId: String, shareId: String, fileNumber: Int) =
        "$boxId/assets/$shareId/asset_$fileNumber"

    private fun getNotificationId() =
        (System.currentTimeMillis() / 1000 + Random.nextInt(1, 100)).toInt()

    private companion object {
        const val MAX_MANIFEST_BYTES = 10L * 1024L * 1024L
    }
}
