package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.databinding.ViewUserInfoPointBinding

class UserPointView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var binding: ViewUserInfoPointBinding

    init {
        inflate(context, R.layout.view_user_info_point, this).apply {
            binding = ViewUserInfoPointBinding.bind(this)
        }

        context.obtainStyledAttributes(attrs, R.styleable.UserPointView).use { typedArray ->
            val iconResource = typedArray.getResourceId(R.styleable.UserPointView_point_icon, 0)
            if (iconResource != 0) {
                binding.imagePointIcon.setImageResource(iconResource)
            }

            val pointNameRes = typedArray.getResourceId(R.styleable.UserPointView_point_name, 0)
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
}