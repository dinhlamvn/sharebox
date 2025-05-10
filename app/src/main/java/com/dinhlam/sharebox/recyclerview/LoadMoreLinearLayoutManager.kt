package com.dinhlam.sharebox.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class LoadMoreLinearLayoutManager(
    context: Context,
    @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL,
    private val willTriggerLoadMore: () -> Boolean,
    private val onLoadMore: () -> Unit
) : LinearLayoutManager(context, orientation, false) {

    private val coroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher() + CoroutineName("load-more-coroutine")
    )
    private var job: Job? = null

    override fun onScrollStateChanged(state: Int) {
        if (job?.isCompleted == false) {
            job?.cancel()
        }

        job = coroutineScope.launch {
            delay(200)
            if (!willTriggerLoadMore.invoke()) {
                return@launch
            }

            if (state == RecyclerView.SCROLL_STATE_IDLE) {
                withContext(Dispatchers.Main) {
                    val lastPosition = findLastCompletelyVisibleItemPosition()
                    if (lastPosition == itemCount - 1) {
                        onLoadMore()
                    }
                }
            }
        }
    }
}