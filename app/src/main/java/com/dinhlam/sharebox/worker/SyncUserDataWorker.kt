package com.dinhlam.sharebox.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.local.entity.User
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.data.repository.UserRepository
import com.dinhlam.sharebox.helper.UserHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncUserDataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userHelper: UserHelper,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val shareRepository: ShareRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result {
        val userInfo = syncUserInfo() ?: return Result.retry()
        realtimeDatabaseRepository.push(userInfo)
        return Result.success()
    }

    private suspend fun syncUserInfo(): User? = withContext(Dispatchers.IO) {
        if (!userHelper.isSignedIn()) {
            return@withContext null
        }

        val currentUserId = userHelper.getCurrentUserId()
        val user = userRepository.findOneRaw(currentUserId) ?: return@withContext null

        val commentCount = commentRepository.countByUser(currentUserId)
        val likeCount = likeRepository.countByUserShare(currentUserId)
        val shareCount = shareRepository.countByUser(currentUserId)

        val drama = commentCount + likeCount * 10 + shareCount * 10
        val level = getLevelByDrama(drama)

        val newUser = user.copy(drama = drama, level = level)
        userRepository.update(newUser)
    }

    private fun getLevelByDrama(drama: Int): Int {
        return when (drama) {
            in 0..1000 -> 0
            in 1001..3000 -> 1
            in 3001..10000 -> 2
            else -> 3
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                1912,
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                    .setContentText(appContext.getString(R.string.sync_data_share_to_cloud))
                    .setSubText(appContext.getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                1912,
                NotificationCompat.Builder(appContext, AppConsts.NOTIFICATION_SYNC_DATA_CHANNEL_ID)
                    .setContentText(appContext.getString(R.string.sync_data_share_to_cloud))
                    .setSubText(appContext.getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).build()
            )
        }
    }
}