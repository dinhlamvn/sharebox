package com.dinhlam.sharebox.receiver

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CustomTabsShareBroadcastReceiver : BaseBroadcastReceiver() {

    companion object {
        const val REQUEST_CODE = 1345
    }

    @Inject
    lateinit var shareRepository: ShareRepository

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var videoHelper: VideoHelper

    override fun onReceive(context: Context?, intent: Intent) {
        val url = intent.dataString ?: return
        val boxId = intent.getStringExtra(AppExtras.EXTRA_BOX_ID)
        coroutineScope.launch {
            val share = shareUrl(null, ShareData.ShareUrl(url), boxId)
            share?.let { insertedShare ->
                WorkerUtils.enqueueSyncShareToCloud(context!!, insertedShare.shareId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.shares_success, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun shareUrl(
        note: String?, shareData: ShareData.ShareUrl, boxId: String?
    ): Share? {
        val isVideoShare = videoHelper.getVideoSource(shareData.url) != null
        return shareRepository.insert(
            shareData = shareData,
            shareNote = note,
            shareBoxId = boxId,
            shareUserId = userHelper.getCurrentUserId(),
            isVideoShare = isVideoShare
        )
    }
}