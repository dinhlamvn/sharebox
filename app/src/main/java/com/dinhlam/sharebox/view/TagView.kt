package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.appcompat.widget.LinearLayoutCompat
import com.dinhlam.sharebox.databinding.ViewTagBinding

class TagView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val binding = ViewTagBinding.inflate(LayoutInflater.from(context), this)

    fun setTagName(tagName: CharSequence?) {
        binding.textTagName.text = tagName
    }

    fun setTagColor(@ColorInt color: Int) {
        binding.iconTag.setIconColor(color)
    }
}