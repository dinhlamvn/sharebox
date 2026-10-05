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

    /** Publish one complete object; readers never combine assets from different exports. */
    suspend fun uploadBoxPackage(boxId: String, file: File): String = withContext(Dispatchers.IO) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: error("Sign in to upload")
        val code = "$uid/$boxId"
        val upload = packageReference(code).putFile(Uri.fromFile(file), StorageMetadata.Builder()
            .setContentType("application/zip").build())
        try { upload.await() } catch (error: kotlinx.coroutines.CancellationException) {
            upload.cancel()
            throw error
        }
        code
    }

    suspend fun downloadBoxPackage(code: String, destination: File): File = withContext(Dispatchers.IO) {
        val reference = packageReference(code)
        require(reference.metadata.await().sizeBytes <= 528L * 1024 * 1024) { "Package is too large" }
        val download = reference.getFile(destination)
        download.addOnProgressListener { progress ->
            if (progress.bytesTransferred > 528L * 1024 * 1024) download.cancel()
        }
        try { download.await() } catch (error: kotlinx.coroutines.CancellationException) {
            download.cancel()
            throw error
        }
        destination
    }

    private fun packageReference(code: String): com.google.firebase.storage.StorageReference {
        val parts = code.split('/')
        require(parts.size == 2 && parts[0].matches(Regex("[A-Za-z0-9_-]{1,128}")) &&
            runCatching { java.util.UUID.fromString(parts[1]).toString() == parts[1] }.getOrDefault(false)) {
            "Paste the full transfer code provided after upload (publisher/box ID)"
        }
        return storage.getReference("box-packages/$code/latest.sharebox")
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

    private fun getNotificationId() =
        (System.currentTimeMillis() / 1000 + Random.nextInt(1, 100)).toInt()

}
