package com.dinhlam.sharebox.recyclerview.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.extensions.cast

class GridItemSpacingDecoration(
    private val spacing: Int,
    private val spanCount: Int
) : RecyclerView.ItemDecoration() {

    init {
        require(spanCount > 0) {
            "$this require spanCount greater than 0"
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        parent.layoutManager?.cast<GridLayoutManager>()
            ?: error("$this only support GridLayoutManager")

        val itemCount = parent.adapter?.itemCount ?: 0

        val spacingValue = spacing / 2
        val viewAdapterPosition = parent.getChildAdapterPosition(view)
        when {
            viewAdapterPosition == 0 || viewAdapterPosition % spanCount == 0 -> {
                outRect.right = spacingValue
            }

            viewAdapterPosition == itemCount - 1 || viewAdapterPosition % spanCount == (spanCount - 1) -> {
                outRect.left = spacingValue
            }

            else -> {
                outRect.left = spacingValue
                outRect.right = spacingValue
            }
        }

        val isNotGridFirstLine = viewAdapterPosition >= spanCount
        if (isNotGridFirstLine) {
            outRect.top = spacing
        }
    }
}