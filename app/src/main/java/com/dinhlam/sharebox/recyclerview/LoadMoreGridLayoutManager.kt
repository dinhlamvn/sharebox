package com.dinhlam.sharebox.recyclerview

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LoadMoreGridLayoutManager(
    context: Context,
    spanCount: Int,
    private val blockShouldLoadMore: () -> Boolean,
    private val onLoadMore: () -> Unit
) : GridLayoutManager(context, spanCount) {

    var hadTriggerLoadMore = false

    override fun onScrollStateChanged(state: Int) {
        if (!blockShouldLoadMore.invoke()) {
            hadTriggerLoadMore = false
            return
        }

        if (itemCount <= 1) {
            return
        }

        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            val lastPosition = findLastCompletelyVisibleItemPosition()
            if (!hadTriggerLoadMore && lastPosition == itemCount - 1) {
                hadTriggerLoadMore = true
                onLoadMore()
            }
        }
    }
}