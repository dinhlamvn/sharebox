package com.dinhlam.sharebox.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dinhlam.sharebox.helper.UserHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncUserDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userHelper: UserHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        userHelper.syncUserInfo()
        return Result.success()
    }
}