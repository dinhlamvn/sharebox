package com.dinhlam.sharebox.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LoadMoreLinearLayoutManager(
    context: Context,
    @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL,
    private val blockShouldLoadMore: () -> Boolean,
    private val onLoadMore: () -> Unit
) : LinearLayoutManager(context, orientation, false) {

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