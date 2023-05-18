package com.dinhlam.sharebox.dialog.bookmarkcollectionpicker

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetViewModelDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.DialogBookmarkCollectionPickerBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.modelview.TextPickerModelView
import com.dinhlam.sharebox.router.AppRouter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BookmarkCollectionPickerDialogFragment :
    BaseBottomSheetViewModelDialogFragment<BookmarkCollectionPickerState, BookmarkCollectionPickerViewModel, DialogBookmarkCollectionPickerBinding>() {

    fun interface OnBookmarkCollectionPickListener {
        fun onBookmarkCollectionDone(bookmarkCollectionIds: String?)
    }

    lateinit var listener: OnBookmarkCollectionPickListener

    @Inject
    lateinit var appRouter: AppRouter

    private val resultLauncherVerifyPasscode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Logger.debug("Hello ok")
            }
        }

    private val resultLauncherCreateBookmarkCollection =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.reloadAfterCreateNewBookmarkCollection()
            }
        }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogBookmarkCollectionPickerBinding {
        return DialogBookmarkCollectionPickerBinding.inflate(inflater, container, false)
    }

    private val adapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isLoading) {
                add(LoadingModelView("loading"))
                return@getState
            }

            if (state.bookmarkCollections.isEmpty()) {
                add(
                    TextModelView(
                        "text_empty",
                        getString(R.string.dialog_bookmark_collection_picker_no_bookmarks)
                    )
                )
                return@getState
            }

            addAll(state.bookmarkCollections.map { bookmarkCollection ->
                TextPickerModelView(
                    "picker_${bookmarkCollection.id}",
                    bookmarkCollection.name,
                    height = 50.dp(),
                    isPicked = state.pickedBookmarkCollectionId == bookmarkCollection.id
                ) {
                    viewModel.onPickBookmarkCollection(bookmarkCollection.id)
                }
            })
        }
    }

    override val viewModel: BookmarkCollectionPickerViewModel by viewModels()

    override fun onStateChanged(state: BookmarkCollectionPickerState) {
        adapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.adapter = adapter

        viewBinding.buttonDone.setOnClickListener {
            getState(viewModel) { state ->
                if (state.pickedBookmarkCollectionId != state.originalPickedBookmarkCollectionId) {
                    listener.onBookmarkCollectionDone(state.pickedBookmarkCollectionId)
                }
                dismiss()
            }
        }

        viewBinding.bottomCreateNew.setOnClickListener {
            resultLauncherCreateBookmarkCollection.launch(
                appRouter.bookmarkCollectionFormIntent(
                    requireContext()
                )
            )
        }
    }
}