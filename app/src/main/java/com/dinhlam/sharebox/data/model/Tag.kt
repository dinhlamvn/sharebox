package com.dinhlam.sharebox.data.model

import androidx.annotation.DrawableRes
import com.dinhlam.sharebox.R

sealed class Tag(val id: Int, val name: String, @DrawableRes val tagResource: Int) {
    object Red : Tag(R.id.tag_red, "Red", R.drawable.ic_tag_red)
    object Green : Tag(R.id.tag_green, "Green", R.drawable.ic_tag_green)
    object Blue : Tag(R.id.tag_blue, "Blue", R.drawable.ic_tag_blue)
    object Yellow : Tag(R.id.tag_yellow, "Yellow", R.drawable.ic_tag_yellow)
    object Gray : Tag(R.id.tag_gray, "Gray", R.drawable.ic_tag_gray)
}
