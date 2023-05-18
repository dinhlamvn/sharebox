package com.dinhlam.sharebox.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.databinding.ViewShareItemBottomActionBinding

class ShareBoxShareItemBottomActionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding: ViewShareItemBottomActionBinding =
        inflate(context, R.layout.view_share_item_bottom_action, this).run {
            ViewShareItemBottomActionBinding.bind(this)
        }

    fun setOnLikeClickListener(listener: OnClickListener?) {
        binding.buttonUpVote.setOnClickListener(listener)
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
        binding.textUpvote.text = resources.getString(R.string.up_vote, number)
    }

    fun setCommentNumber(number: Int) {
        binding.textComment.text = resources.getString(R.string.comment, number)
    }

    fun updateBookmarkStatus(isBookmarked: Boolean) {
        binding.buttonBookmark.setImageResource(
            if (isBookmarked) {
                R.drawable.ic_bookmarked
            } else {
                R.drawable.ic_bookmark
            }
        )
    }
}