package com.dinhlam.sharebox.recyclerview.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.extensions.cast

class HorizontalSpacingDecoration(
    private val spacing: Int,
    private val gridConfig: GridConfig = GridConfig.None
) : RecyclerView.ItemDecoration() {

    sealed class GridConfig {
        data object None : GridConfig()
        data class GridSpanCount(val spanCount: Int) : GridConfig()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        parent.layoutManager?.cast<LinearLayoutManager>()?.takeIf { linearLayoutManager ->
            linearLayoutManager.orientation == LinearLayoutManager.HORIZONTAL
        } ?: error("$this only support LinearLayoutManager with HORIZONTAL orientation")

        val itemCount = parent.adapter?.itemCount ?: 0

        val spacingValue = spacing / 2
        val viewAdapterPosition = parent.getChildAdapterPosition(view)
        when (viewAdapterPosition) {
            0 -> {
                outRect.right = spacingValue
            }

            itemCount - 1 -> {
                outRect.left = spacingValue
            }

            else -> {
                outRect.left = spacingValue
                outRect.right = spacingValue
            }
        }
    }
}