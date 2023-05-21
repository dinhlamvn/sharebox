package com.dinhlam.sharebox.ui.home.bookmark

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.databinding.FragmentBookmarkBinding
import com.dinhlam.sharebox.dialog.singlechoice.SingleChoiceBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.BookmarkHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.modelview.bookmark.BookmarkCollectionModelView
import com.dinhlam.sharebox.router.AppRouter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BookmarkFragment :
    BaseViewModelFragment<BookmarkState, BookmarkViewModel, FragmentBookmarkBinding>(),
    SingleChoiceBottomSheetDialogFragment.OnOptionItemSelectedListener {

    private val bookmarkCollectionFormResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.doOnRefresh()
            }
        }

    private val requestPasscodeEditResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bookmarkCollection =
                    result.data?.getParcelableExtraCompat<BookmarkCollectionDetail>(AppExtras.EXTRA_BOOKMARK_COLLECTION)
                        ?: return@registerForActivityResult
                bookmarkCollectionFormResultLauncher.launch(
                    appRouter.bookmarkCollectionFormIntent(
                        requireContext(), bookmarkCollection
                    )
                )
            }
        }

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
            }

            if (state.bookmarkCollections.isEmpty() && !state.isRefreshing) {
                add(TextModelView("text_empty", getString(R.string.no_bookmark_collections)))
            } else if (state.bookmarkCollections.isNotEmpty()) {
                addAll(state.bookmarkCollections.mapIndexed { idx, bookmarkCollection ->
                    BookmarkCollectionModelView(
                        bookmarkCollection.id,
                        bookmarkCollection.name,
                        bookmarkCollection.thumbnail,
                        bookmarkCollection.desc,
                        bookmarkCollection.passcode,
                        bookmarkCollection.shareCount,
                        if (idx % COLLECTION_SPAN_COUNT == 0) 0 else 8.dp(),
                        if (idx >= COLLECTION_SPAN_COUNT) 8.dp() else 0,
                        BaseListAdapter.NoHashProp(View.OnClickListener {
                            showOptionMenu(bookmarkCollection.id)
                        })
                    )
                })
            }
        }
    }

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var bookmarkHelper: BookmarkHelper

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

        viewBinding.buttonAdd.setOnClickListener {
            bookmarkCollectionFormResultLauncher.launch(
                appRouter.bookmarkCollectionFormIntent(requireContext())
            )
        }
    }

    private fun showOptionMenu(bookmarkCollectionId: String) {
        getState(viewModel) { state ->
            val collectionDetail =
                state.findCollectionDetail(bookmarkCollectionId) ?: return@getState
            bookmarkHelper.showOptionMenu(
                childFragmentManager,
                resources.getStringArray(R.array.bookmark_collection_option_menu_items),
                bundleOf(AppExtras.EXTRA_BOOKMARK_COLLECTION to collectionDetail)
            )
        }
    }

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        val bookmarkCollection =
            args.getParcelableExtraCompat<BookmarkCollectionDetail>(AppExtras.EXTRA_BOOKMARK_COLLECTION)
                ?: return
        Logger.debug("Hello $position --- $bookmarkCollection")
        when (position) {
            0 -> startActivity(
                appRouter.bookmarkListItemIntent(
                    requireContext(), bookmarkCollection.id
                )
            )

            1 -> {
                val passcode = bookmarkCollection.passcode.takeIfNotNullOrBlank()
                    ?: return bookmarkCollectionFormResultLauncher.launch(
                        appRouter.bookmarkCollectionFormIntent(
                            requireContext(), bookmarkCollection
                        )
                    )
                requestPasscodeEditResultLauncher.launch(appRouter.passcodeIntent(
                    requireContext(), passcode
                ).apply {
                    putExtra(AppExtras.EXTRA_BOOKMARK_COLLECTION, bookmarkCollection)
                })
            }
        }
    }
}