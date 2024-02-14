package com.dinhlam.sharebox.receiver

import android.content.BroadcastReceiver
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

abstract class BaseBroadcastReceiver : BroadcastReceiver() {
    protected val coroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher() + CoroutineName("Broadcast Receiver Scope")
    )
}