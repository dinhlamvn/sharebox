package com.dinhlam.sharebox.ui.home.bookmark.list

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsControllerCompat
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.databinding.ActivityBookmarkListItemBinding
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.modelview.TextModelView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.absoluteValue

@AndroidEntryPoint
class BookmarkListItemActivity :
    BaseViewModelActivity<BookmarkListItemState, BookmarkListItemViewModel, ActivityBookmarkListItemBinding>() {

    override fun onCreateViewBinding(): ActivityBookmarkListItemBinding {
        return ActivityBookmarkListItemBinding.inflate(layoutInflater)
    }

    private val shareAdapter = BaseListAdapter.createAdapter {
        add(TextModelView("text_empty", "No shares"))
    }

    override val viewModel: BookmarkListItemViewModel by viewModels()

    override fun onStateChanged(state: BookmarkListItemState) {
        state.bookmarkCollection?.let(::updateUi)
        shareAdapter.requestBuildModelViews()
    }

    private fun updateUi(bookmarkCollection: BookmarkCollectionDetail) {
        ImageLoader.instance.load(this, bookmarkCollection.thumbnail, viewBinding.imageTopBar)
        ImageLoader.instance.load(
            this, bookmarkCollection.thumbnail, viewBinding.imageThumbnailSmall
        ) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }
        viewBinding.toolbar.title = bookmarkCollection.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.appbar.addOnOffsetChangedListener { appBar, verticalOffset ->
            val eightyPercent = appBar.totalScrollRange * 0.8
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                verticalOffset.absoluteValue >= eightyPercent
            viewBinding.imageThumbnailSmall.alpha =
                verticalOffset.absoluteValue / eightyPercent.toFloat()
        }

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewBinding.recyclerView.adapter = shareAdapter
    }
}