package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.databinding.ViewBoxSectionButtonBinding
import com.dinhlam.sharebox.extensions.dpF
import com.dinhlam.sharebox.extensions.getColorCompat
import com.google.android.material.card.MaterialCardView

class BoxSectionButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.style.AppMaterialCardView
) : MaterialCardView(context, attrs, defStyle) {

    private val binding = ViewBoxSectionButtonBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        radius = 12.dpF
        cardElevation = 4.dpF
    }

    fun setBoxName(name: CharSequence?) {
        if (name != null) {
            binding.textBoxName.setTextAppearance(context, R.style.TextBodyMedium)
            binding.textBoxName.text = name
        } else {
            binding.textBoxName.setTextColor(context.getColorCompat(R.color.grey_400))
            binding.textBoxName.setText(R.string.no_box_selected)
        }
    }

    fun showLock(showLock: Boolean) {
        binding.iconLock.isVisible = showLock
    }

    fun showAddIcon(showAdd: Boolean) {
        binding.iconAdd.isVisible = showAdd
    }

    fun setOnAddIconClickListener(listener: OnClickListener?) {
        binding.iconAdd.setOnClickListener(listener)
    }

    fun playZoomAnimation() {
        startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.zoom_in
            )
        )
    }
}