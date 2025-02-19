package com.dinhlam.sharebox.dialog.action

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetViewModelDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogFragmentBottomSheetShareActionBinding
import com.dinhlam.sharebox.databinding.DialogLayoutInputBinding
import com.dinhlam.sharebox.dialog.tag.TagPickerDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.listmodel.IconTextListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.router.Router
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetShareActionDialogFragment :
    BaseBottomSheetViewModelDialogFragment<BottomSheetShareActionState, BottomSheetShareActionViewModel, DialogFragmentBottomSheetShareActionBinding>() {

    override val viewModel: BottomSheetShareActionViewModel by viewModels()

    override fun onStateChanged(state: BottomSheetShareActionState) {
        binding.loading.toggle(state.asyncUpdate is BaseViewModel.AsyncLoad.Loading)
        listAdapter.requestBuildListModels()
    }

    companion object {

        @JvmStatic
        fun showDialog(
            fragmentManager: FragmentManager,
            shareId: String
        ): BottomSheetShareActionDialogFragment {
            return BottomSheetShareActionDialogFragment().apply {
                arguments = bundleOf(
                    AppExtras.EXTRA_SHARE_ID to shareId
                )
            }.also { dialog ->
                dialog.show(fragmentManager, "BottomSheetShareActionDialogFragment")
            }
        }
    }

    private val moveShareToBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                viewModel.moveShareToBox(boxId)
            }
        }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var router: Router

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFragmentBottomSheetShareActionBinding {
        return DialogFragmentBottomSheetShareActionBinding.inflate(inflater, container, false)
    }

    private val listAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.shareDetail == null) {
                LoadingListModel("loading", height = 100).attachTo(this)
                return@getState
            }

            state.actions.forEachIndexed { index, action ->
                IconTextListModel(
                    "choice_$index",
                    action.icon,
                    action.text,
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        onActionClick(action.actionId)
                    })
                ).attachTo(this)
            }
        }
    }

    private fun onActionClick(actionId: BottomSheetShareActionState.ActionId) {
        val share =
            getState(viewModel, BottomSheetShareActionState::shareDetail) ?: return showToast(
                R.string.share_not_found
            )

        when (actionId) {
            BottomSheetShareActionState.ActionId.SHARE_TO -> shareToOther(share)
            BottomSheetShareActionState.ActionId.EDIT_NOTE -> showDialogInputShareNote(share.shareNote)
            BottomSheetShareActionState.ActionId.MOVE_TO_OTHER_BOX -> onRequestMoveShare()
            BottomSheetShareActionState.ActionId.COPY -> copyShare(share)
            BottomSheetShareActionState.ActionId.DOWNLOAD -> downloadShare(share)
            BottomSheetShareActionState.ActionId.TAGS -> onTags(share.shareId)
            BottomSheetShareActionState.ActionId.VIEW_TAGS -> onViewTags(share.tagId)
            BottomSheetShareActionState.ActionId.COPY_BOX_ID -> copyBoxID(share)
            BottomSheetShareActionState.ActionId.MOVE_TO_TRASH -> moveToTrash(share)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter.attachTo(binding.recyclerView, viewLifecycleOwner)

        onChange(BottomSheetShareActionState::shareDetail) { shareDetail ->
            viewModel.buildActions(requireContext(), shareDetail)
        }

        onChange(BottomSheetShareActionState::asyncUpdate) { asyncLoad ->
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                dismiss()
            } else if (asyncLoad is BaseViewModel.AsyncLoad.Failed) {
                showToast(asyncLoad.error.message)
            }
        }
    }

    private fun shareToOther(share: ShareDetail) {
        shareHelper.shareToOther(share)
        dismiss()
    }

    private fun showDialogInputShareNote(shareNote: String?) {
        val binding = DialogLayoutInputBinding.inflate(LayoutInflater.from(requireContext()))
        binding.dialogTextInputEdit.inputType = InputType.TYPE_CLASS_TEXT
        binding.dialogTextInputEdit.hint = getString(R.string.hint_note)
        binding.dialogTextInputEdit.setText(shareNote)
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.share_note)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                val note = binding.dialogTextInputEdit.text?.trimmedString()
                viewModel.saveShareNote(note)
            }
            .setNegativeButton(R.string.cancel, null)
            .setView(binding.root)
            .show()
    }

    private fun onRequestMoveShare() {
        moveShareToBoxLauncher.launch(
            router.boxList(
                requireContext(),
                getString(R.string.move_share_to)
            )
        )
    }

    private fun copyShare(share: ShareDetail) {
        val content = share.shareData.cast<ShareData.ShareText>()?.text
            ?: share.shareData.cast<ShareData.ShareUrl>()?.url
            ?: return showToast(R.string.nothing_to_copy)
        context?.copy(content)
        dismiss()
    }

    private fun downloadShare(share: ShareDetail) {
        shareHelper.downloadShareContent(requireContext(), share)
        dismiss()
    }

    private fun copyBoxID(share: ShareDetail) {
        context?.copy(share.boxDetail?.boxId)
        dismiss()
    }

    private fun onTags(shareId: String) {
        TagPickerDialogFragment.showDialog(childFragmentManager, shareId)
    }

    private fun onViewTags(tagId: Int?) {
        startActivity(router.tags(requireContext(), tagId))
        dismiss()
    }

    private fun moveToTrash(share: ShareDetail) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.confirm_move_to_trash)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.moveShareToTrash(share.shareId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}