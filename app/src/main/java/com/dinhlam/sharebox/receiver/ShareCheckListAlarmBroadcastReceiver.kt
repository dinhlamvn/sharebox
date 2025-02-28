package com.dinhlam.sharebox.receiver

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.isNotZero
import com.dinhlam.sharebox.extensions.pushNotification
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class ShareCheckListAlarmBroadcastReceiver : BaseBroadcastReceiver() {

    @Inject
    lateinit var shareRepository: ShareRepository

    @Inject
    lateinit var router: Router

    override fun onReceive(context: Context, intent: Intent) {
        val shareId = intent.getStringExtra(AppExtras.EXTRA_SHARE_ID) ?: return
        val position = intent.getIntExtra(AppExtras.EXTRA_POSITION, -1)
        if (position < 0) {
            return
        }

        coroutineScope.launch(Dispatchers.IO) {
            val share = shareRepository.findOne(shareId) ?: return@launch
            val shareData = share.shareData.cast<ShareData.ShareCheckList>() ?: return@launch
            val checkListData = shareData.checkListDataList.getOrNull(position) ?: return@launch
            context.pushNotification(
                Random.nextInt(),
                createNotification(
                    context,
                    share.shareId,
                    share.shareNote,
                    checkListData.title,
                    checkListData.datetime
                )
            )
        }
    }

    private fun createNotification(
        context: Context,
        shareId: String,
        title: String?,
        subtitle: String,
        datetime: Long
    ): Notification {
        val contentText = buildString {
            append(subtitle)
            if (datetime.isNotZero) {
                append("\n")
                append(context.getString(R.string.datetime, datetime.format("dd/MM/yyyy, HH:mm")))
            }
        }

        val intent = router.checkList(context, shareId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        return NotificationCompat.Builder(context, AppConsts.NOTIFICATION_DEFAULT_CHANNEL_ID)
            .setContentTitle(title ?: context.getString(R.string.checklist))
            .setAutoCancel(true)
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }
}