package com.dinhlam.sharebox.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.databinding.ViewShareItemBottomActionBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.utils.IconUtils

class ShareBoxShareItemBottomActionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding: ViewShareItemBottomActionBinding =
        inflate(context, R.layout.view_share_item_bottom_action, this).run {
            ViewShareItemBottomActionBinding.bind(this)
        }

    init {
        binding.textLike.setDrawableCompat(start = IconUtils.likeIcon(context))
        binding.textComment.setDrawableCompat(start = IconUtils.commentIcon(context))
        binding.buttonShare.setImageDrawable(IconUtils.shareIcon(context))
    }

    fun setLikeIcon(icon: Drawable) {
        binding.textLike.setDrawableCompat(start = icon)
    }

    fun setCommentIcon(icon: Drawable) {
        binding.textComment.setDrawableCompat(start = icon)
    }

    fun setShareIcon(icon: Drawable) {
        binding.buttonShare.setImageDrawable(icon)
    }

    fun setBookmarkIcon(icon: Drawable) {
        binding.buttonBookmark.setImageDrawable(icon)
    }

    fun setLikeTextColor(@ColorInt color: Int) {
        binding.textLike.setTextColor(color)
    }

    fun setCommentTextColor(@ColorInt color: Int) {
        binding.textComment.setTextColor(color)
    }

    fun setOnLikeClickListener(listener: OnClickListener?) {
        binding.buttonLike.setOnClickListener(listener)
    }

    fun setOnCommentClickListener(listener: OnClickListener?) {
        binding.buttonComment.setOnClickListener(listener)
    }

    fun setOnShareClickListener(listener: OnClickListener?) {
        binding.buttonShare.setOnClickListener(listener)
    }

    fun setOnBookmarkClickListener(listener: OnClickListener?) {
        binding.buttonBookmark.setOnClickListener(listener)
    }

    fun setLikeNumber(number: Int) {
        binding.textLike.text = resources.getString(R.string.like, number)
    }

    fun setCommentNumber(number: Int) {
        binding.textComment.text = resources.getString(R.string.comment, number)
    }

    fun release() {
        binding.textComment.text = null
        binding.textLike.text = null
    }
}