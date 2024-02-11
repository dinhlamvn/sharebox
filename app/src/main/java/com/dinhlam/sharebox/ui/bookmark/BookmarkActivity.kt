package com.dinhlam.sharebox.ui.bookmark

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityBookmarkBinding
import com.dinhlam.sharebox.dialog.singlechoice.SingleChoiceBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.BookmarkHelper
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.bookmark.BookmarkCollectionListModel
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.router.Router
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BookmarkActivity :
    BaseViewModelActivity<BookmarkState, BookmarkViewModel, ActivityBookmarkBinding>(),
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
                    router.bookmarkCollectionFormIntent(
                        this, bookmarkCollection
                    )
                )
            }
        }

    private val requestPasscodeDeleteResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bookmarkCollectionId =
                    result.data?.getStringExtra(AppExtras.EXTRA_BOOKMARK_COLLECTION_ID)
                        ?: return@registerForActivityResult
                viewModel.deleteBookmarkCollection(bookmarkCollectionId)
            }
        }

    companion object {
        private const val COLLECTION_SPAN_COUNT = 2
    }

    override fun onCreateViewBinding(): ActivityBookmarkBinding {
        return ActivityBookmarkBinding.inflate(layoutInflater)
    }

    private val collectionAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                LoadingListModel("loading_collections").attachTo(this)
            }

            if (state.bookmarkCollections.isEmpty() && !state.isRefreshing) {
                TextListModel("text_empty", getString(R.string.no_bookmark_collections)).attachTo(this)
            } else if (state.bookmarkCollections.isNotEmpty()) {
                state.bookmarkCollections.forEachIndexed { idx, bookmarkCollection ->
                    BookmarkCollectionListModel(
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
                    ).attachTo(this)
                }
            }
        }
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var bookmarkHelper: BookmarkHelper

    override val viewModel: BookmarkViewModel by viewModels()

    override fun onStateChanged(state: BookmarkState) {
        collectionAdapter.requestBuildModelViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.recyclerView.layoutManager =
            GridLayoutManager(this, COLLECTION_SPAN_COUNT).apply {
                spanSizeLookup = BaseSpanSizeLookup(collectionAdapter, COLLECTION_SPAN_COUNT)
            }
        binding.recyclerView.adapter = collectionAdapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnRefresh()
        }

        binding.buttonAdd.setOnClickListener {
            bookmarkCollectionFormResultLauncher.launch(
                router.bookmarkCollectionFormIntent(this)
            )
        }
    }

    private fun showOptionMenu(bookmarkCollectionId: String) {
        getState(viewModel) { state ->
            val collectionDetail =
                state.findCollectionDetail(bookmarkCollectionId) ?: return@getState

            val arrayIcons = arrayOf(
                R.drawable.ic_open, R.drawable.ic_edit, R.drawable.ic_delete
            )
            val choiceItems =
                resources.getStringArray(R.array.bookmark_collection_option_menu_items)
                    .mapIndexed { index, text ->
                        SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(
                            arrayIcons[index], text
                        )
                    }.toTypedArray()

            bookmarkHelper.showOptionMenu(
                supportFragmentManager,
                choiceItems,
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
                router.bookmarkListItemIntent(
                    this, bookmarkCollection.id
                )
            )

            1 -> {
                val passcode = bookmarkCollection.passcode.takeIfNotNullOrBlank()
                    ?: return bookmarkCollectionFormResultLauncher.launch(
                        router.bookmarkCollectionFormIntent(
                            this, bookmarkCollection
                        )
                    )
                requestPasscodeEditResultLauncher.launch(router.passcodeIntent(
                    this, passcode
                ).apply {
                    putExtra(AppExtras.EXTRA_BOOKMARK_COLLECTION, bookmarkCollection)
                })
            }

            2 -> {
                MaterialAlertDialogBuilder(this).setTitle(R.string.confirmation)
                    .setMessage(R.string.bookmark_collection_delete_confirm_message)
                    .setPositiveButton(R.string.dialog_ok) { _, _ ->
                        val passcode = bookmarkCollection.passcode.takeIfNotNullOrBlank()
                            ?: return@setPositiveButton viewModel.deleteBookmarkCollection(
                                bookmarkCollection.id
                            )
                        requestPasscodeDeleteResultLauncher.launch(router.passcodeIntent(
                            this, passcode
                        ).apply {
                            putExtra(
                                AppExtras.EXTRA_BOOKMARK_COLLECTION_ID, bookmarkCollection.id
                            )
                        })
                    }.setNegativeButton(R.string.dialog_cancel, null)
                    .show()
            }
        }
    }
}