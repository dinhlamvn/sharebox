package com.dinhlam.sharebox.recyclerview.decoration

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dpF
import com.dinhlam.sharebox.extensions.takeIfNotZero

class HorizontalCirclePagerItemDecoration constructor(
    private val strokeWidth: Float = 1.dpF(),
    @ColorInt val colorActive: Int = Color.BLACK,
    @ColorInt val colorInactive: Int = Color.WHITE
) : RecyclerView.ItemDecoration() {

    private val radius: Float = 5.dpF()
    private val spacing: Float = 5.dpF()

    private val paint by lazyOf(Paint().apply {
        strokeWidth = this@HorizontalCirclePagerItemDecoration.strokeWidth
        isAntiAlias = true
        style = Paint.Style.FILL
    })


    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val totalItem = parent.adapter?.itemCount?.takeIfNotZero() ?: return
        val width = parent.width
        val height = parent.height

        val indicatorWidth = totalItem * radius * 2 + spacing.times(totalItem - 1)

        if (indicatorWidth >= width) {
            return
        }

        val xPos = width.minus(indicatorWidth).div(2)
        val yPos = height.minus(16.dpF())

        val layoutManager = parent.layoutManager.cast<LinearLayoutManager>() ?: return
        val currentPosition = layoutManager.findFirstVisibleItemPosition()
        if (currentPosition == RecyclerView.NO_POSITION) {
            return
        }

        drawIndicator(c, xPos, yPos, totalItem, currentPosition)
    }

    private fun drawIndicator(
        canvas: Canvas,
        startX: Float,
        yPos: Float,
        totalItem: Int,
        currentPosition: Int
    ) {
        var r: Float
        // Left indicator
        paint.color = colorInactive
        for (i in 0 until currentPosition) {
            r = radius * ((i + 1f) / (currentPosition + 1)).coerceAtLeast(0.3f)
            canvas.drawCircle(startX + calcCoordinateX(i), yPos, r, paint)
        }

        // Active indicator
        paint.color = colorActive
        r = radius * 1.25f
        canvas.drawCircle(startX + calcCoordinateX(currentPosition), yPos, r, paint)

        // Right indicator
        paint.color = colorInactive
        for (i in currentPosition + 1 until totalItem) {
            r = radius * (1 - (i * 1f) / (totalItem)).coerceAtLeast(0.3f)
            canvas.drawCircle(startX + calcCoordinateX(i), yPos, r, paint)
        }
    }

    private fun calcCoordinateX(position: Int): Float {
        return radius * 2 * position + position * spacing
    }
}