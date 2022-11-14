package com.dinhlam.sharesaver.model

import android.graphics.Color
import androidx.annotation.ColorInt
import com.dinhlam.sharesaver.R

sealed class Tag(val id: Int, val name: String, @ColorInt val color: Int) {
    object Red : Tag(R.id.tag_red, "Red", Color.RED)
    object Green : Tag(R.id.tag_green, "Green", Color.GREEN)
    object Blue : Tag(R.id.tag_blue, "Blue", Color.BLUE)
    object Yellow : Tag(R.id.tag_yellow, "Yellow", Color.YELLOW)
    object Gray : Tag(R.id.tag_gray, "Gray", Color.GRAY)
}
