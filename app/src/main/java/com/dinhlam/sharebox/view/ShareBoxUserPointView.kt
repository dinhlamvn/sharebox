package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.databinding.ViewUserInfoPointBinding

class ShareBoxUserPointView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding: ViewUserInfoPointBinding =
        inflate(context, R.layout.view_user_info_point, this).run {
            ViewUserInfoPointBinding.bind(this)
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ShareBoxUserPointView).use { typedArray ->
            val iconCode =
                typedArray.getString(R.styleable.ShareBoxUserPointView_icon_code).orEmpty()
            binding.icon.setIconCode(iconCode)

            val pointNameRes =
                typedArray.getResourceId(R.styleable.ShareBoxUserPointView_point_name, 0)
            if (pointNameRes != 0) {
                binding.textPointName.text = context.getString(pointNameRes)
            }
        }
    }

    fun setPointText(pointText: String) {
        binding.textPointNumber.text = pointText
    }

    fun setPointNameText(pointNameText: String) {
        binding.textPointName.text = pointNameText
    }

    fun setPointIcon(iconCode: String) {
        binding.icon.setIconCode(iconCode)
    }
}