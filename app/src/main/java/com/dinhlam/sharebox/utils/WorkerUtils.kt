package com.dinhlam.sharebox.utils

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dinhlam.sharebox.services.CleanUpWorker

object WorkerUtils {

    fun enqueueCleanUpOldData(context: Context) {
        val cleanUpWorkerRequest = OneTimeWorkRequestBuilder<CleanUpWorker>()
            .build()
        WorkManager.getInstance(context).enqueue(cleanUpWorkerRequest)
    }
}