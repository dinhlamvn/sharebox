package com.dinhlam.sharebox.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LoadMoreLinearLayoutManager(
    context: Context,
    @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL,
    private val blockShouldLoadMore: () -> Boolean,
    private val block: () -> Unit
) : LinearLayoutManager(context, orientation, false) {

    var isLoadMore = false

    override fun onScrollStateChanged(state: Int) {
        if (!blockShouldLoadMore.invoke()) {
            return
        }
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            val lastPosition = findLastCompletelyVisibleItemPosition()
            if (itemCount > 0 && !isLoadMore && lastPosition == itemCount - 1) {
                isLoadMore = true
                block()
            }
        }
    }
}