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
import com.dinhlam.sharebox.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.databinding.DialogBookmarkCollectionPickerBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeWithEllipsizeEnd
import com.dinhlam.sharebox.modelview.LoadingListModel
import com.dinhlam.sharebox.modelview.TextListModel
import com.dinhlam.sharebox.modelview.TextPickerListModel
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BookmarkCollectionPickerDialogFragment :
    BaseBottomSheetViewModelDialogFragment<BookmarkCollectionPickerState, BookmarkCollectionPickerViewModel, DialogBookmarkCollectionPickerBinding>() {

    fun interface OnBookmarkCollectionPickListener {
        fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?)
    }

    @Inject
    lateinit var router: Router

    private val resultLauncherVerifyPasscode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                getState(viewModel) { state ->
                    getListener()?.onBookmarkCollectionDone(
                        state.shareId, state.pickedBookmarkCollection?.id
                    )
                    dismiss()
                }
            }
        }

    private val resultLauncherVerifyOriginalPasscode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                doAfterOriginalPasscodeVerified()
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
                add(LoadingListModel("loading"))
                return@getState
            }

            if (state.bookmarkCollections.isEmpty()) {
                add(
                    TextListModel(
                        "text_empty",
                        getString(R.string.dialog_bookmark_collection_picker_no_bookmarks)
                    )
                )
                return@getState
            }

            addAll(state.bookmarkCollections.map { bookmarkCollection ->
                TextPickerListModel(
                    "picker_${bookmarkCollection.id}",
                    bookmarkCollection.name,
                    height = 50.dp(),
                    startIcon = if (bookmarkCollection.passcode == null) null else Icons.lockIcon(
                        requireContext()
                    ) { copy(sizeDp = 20) },
                    pickedIcon = Icons.doneIcon(requireContext()) { copy(sizeDp = 20) },
                    isPicked = state.pickedBookmarkCollection?.id == bookmarkCollection.id,
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        viewModel.onPickBookmarkCollection(bookmarkCollection)
                    })
                )
            })
        }
    }

    override val viewModel: BookmarkCollectionPickerViewModel by viewModels()

    override fun onStateChanged(state: BookmarkCollectionPickerState) {
        adapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bottomCreateNew.setDrawableCompat(Icons.plusIcon(requireContext()) {
            copy(sizeDp = 20)
        })

        binding.buttonDone.setDrawableCompat(Icons.doneIcon(requireContext()) {
            copy(sizeDp = 20)
        })

        binding.recyclerView.adapter = adapter

        binding.buttonDone.setOnClickListener {
            getState(viewModel) { state ->
                if (state.pickedBookmarkCollection?.id != state.originalBookmarkCollection?.id) {
                    state.originalBookmarkCollection?.takeIf { collection -> !collection.passcode.isNullOrBlank() }
                        ?.let(::requestVerifyOriginalPasscode) ?: doAfterOriginalPasscodeVerified()
                } else {
                    dismiss()
                }
            }
        }

        binding.bottomCreateNew.setOnClickListener {
            resultLauncherCreateBookmarkCollection.launch(
                router.bookmarkCollectionFormIntent(
                    requireContext()
                )
            )
        }
    }

    private fun requestVerifyOriginalPasscode(bookmarkCollectionDetail: BookmarkCollectionDetail) {
        resultLauncherVerifyOriginalPasscode.launch(
            router.passcodeIntent(
                requireContext(), bookmarkCollectionDetail.passcode!!, getString(
                    R.string.dialog_bookmark_collection_picker_verify_passcode,
                    bookmarkCollectionDetail.name.takeWithEllipsizeEnd(10)
                )
            )
        )
    }

    private fun doAfterOriginalPasscodeVerified() = getState(viewModel) { state ->
        val bookmarkCollectionName = state.pickedBookmarkCollection?.name.orEmpty()
        val passcode = state.pickedBookmarkCollection?.passcode ?: return@getState run {
            getListener()?.onBookmarkCollectionDone(
                state.shareId, state.pickedBookmarkCollection?.id
            )
            dismiss()
        }
        resultLauncherVerifyPasscode.launch(
            router.passcodeIntent(
                requireContext(), passcode, getString(
                    R.string.dialog_bookmark_collection_picker_verify_passcode,
                    bookmarkCollectionName.takeWithEllipsizeEnd(10)
                )
            )
        )
    }

    private fun getListener(): OnBookmarkCollectionPickListener? {
        return parentFragment.cast() ?: activity.cast()
    }
}