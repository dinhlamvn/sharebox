package com.dinhlam.sharebox.recyclerview

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LoadMoreGridLayoutManager(
    context: Context,
    spanCount: Int,
    private val isLoadingMore: () -> Boolean,
    private val onLoadMore: () -> Unit
) : GridLayoutManager(context, spanCount) {
    private var lastTimeTriggerLoadMore: Long = 0L

    override fun onScrollStateChanged(state: Int) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            if (isLoadingMore()) {
                return
            }

            // Do not trigger load more within 1s to avoid redundant call
            if (System.currentTimeMillis() - lastTimeTriggerLoadMore <= 1000L) {
                return
            }

            if (itemCount <= 1) {
                return
            }

            val lastPosition = findLastCompletelyVisibleItemPosition()
            if (lastPosition == itemCount - 1) {
                lastTimeTriggerLoadMore = System.currentTimeMillis()
                onLoadMore()
            }
        }
    }
}