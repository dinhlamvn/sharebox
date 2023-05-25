package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.databinding.ViewShareItemBottomActionBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.imageloader.ImageLoader

class ShareBoxShareItemBottomActionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding: ViewShareItemBottomActionBinding =
        inflate(context, R.layout.view_share_item_bottom_action, this).run {
            ViewShareItemBottomActionBinding.bind(this)
        }

    fun setVoteIcon(@DrawableRes icon: Int) {
        binding.textVote.setDrawableCompat(start = icon)
    }

    fun setCommentIcon(@DrawableRes icon: Int) {
        binding.textComment.setDrawableCompat(start = icon)
    }

    fun setShareIcon(@DrawableRes icon: Int) {
        ImageLoader.instance.load(context, icon, binding.buttonShare)
    }

    fun setBookmarkIcon(@DrawableRes icon: Int) {
        ImageLoader.instance.load(context, icon, binding.buttonBookmark)
    }

    fun setVoteTextColor(@ColorInt color: Int) {
        binding.textVote.setTextColor(color)
    }

    fun setCommentTextColor(@ColorInt color: Int) {
        binding.textComment.setTextColor(color)
    }

    fun setOnLikeClickListener(listener: OnClickListener?) {
        binding.buttonVote.setOnClickListener(listener)
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

    fun setVoteNumber(number: Int) {
        binding.textVote.text = resources.getString(R.string.up_vote, number)
    }

    fun setCommentNumber(number: Int) {
        binding.textComment.text = resources.getString(R.string.comment, number)
    }

    fun release() {
        binding.textComment.text = null
        binding.textVote.text = null
    }
}