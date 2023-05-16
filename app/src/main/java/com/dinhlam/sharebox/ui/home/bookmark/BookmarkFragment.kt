package com.dinhlam.sharebox.ui.home.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.databinding.FragmentBookmarkBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.modelview.bookmark.BookmarkCollectionModelView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment :
    BaseViewModelFragment<BookmarkState, BookmarkViewModel, FragmentBookmarkBinding>() {

    companion object {
        private const val COLLECTION_SPAN_COUNT = 2
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentBookmarkBinding {
        return FragmentBookmarkBinding.inflate(inflater, container, false)
    }

    private val collectionAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView("loading_collections"))
                return@getState
            }

            if (state.bookmarkCollections.isEmpty()) {
                add(TextModelView(getString(R.string.no_bookmark_collections)))
            } else {
                addAll(state.bookmarkCollections.mapIndexed { idx, bookmarkCollection ->
                    BookmarkCollectionModelView(
                        bookmarkCollection.id,
                        bookmarkCollection.name,
                        bookmarkCollection.thumbnail,
                        bookmarkCollection.desc,
                        if (idx % COLLECTION_SPAN_COUNT == 0) 0 else 8.dp(),
                        if (idx >= COLLECTION_SPAN_COUNT) 8.dp() else 0,
                    )
                })
            }
        }
    }


    override val viewModel: BookmarkViewModel by viewModels()

    override fun onStateChanged(state: BookmarkState) {
        collectionAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.layoutManager =
            GridLayoutManager(requireContext(), COLLECTION_SPAN_COUNT).apply {
                spanSizeLookup = BaseSpanSizeLookup(collectionAdapter, COLLECTION_SPAN_COUNT)
            }
        viewBinding.recyclerView.adapter = collectionAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewBinding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnRefresh()
        }
    }
}