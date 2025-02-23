package com.dinhlam.sharebox.dialog.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogFragmentTagPickerBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.listmodel.TagItemListModel
import com.dinhlam.sharebox.recyclerview.decoration.GridItemSpacingDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagPickerDialogFragment :
    BaseViewModelDialogFragment<TagPickerState, TagPickerViewModel, DialogFragmentTagPickerBinding>() {

    companion object {
        @JvmStatic
        fun showDialog(fragmentManager: FragmentManager, shareId: String): TagPickerDialogFragment {
            return TagPickerDialogFragment().apply {
                arguments = bundleOf(AppExtras.EXTRA_SHARE_ID to shareId)
                show(fragmentManager, "dialog_tag_picker")
            }
        }
    }

    override val isUseMaterialDialog: Boolean
        get() = false

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentTagPickerBinding {
        return DialogFragmentTagPickerBinding.inflate(inflater, container, false)
    }

    override val viewModel: TagPickerViewModel by viewModels()

    private val tagAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            state.tags.forEach { tag ->
                TagItemListModel(
                    "tag_${tag.id}",
                    tag.tagColor,
                    state.tagIdPicked == tag.id,
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        viewModel.setSelectedTag(tag.id)
                    })
                ).attachTo(this)
            }
        }
    }

    override fun onStateChanged(state: TagPickerState) {
        tagAdapter.requestBuildListModels()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.addItemDecoration(GridItemSpacingDecoration(8.dp, 5))
        tagAdapter.attachTo(binding.recyclerView)

        onAsyncChange(TagPickerState::asyncLoadSaveTag, onFail = { error ->
            showToast(error.message)
        }) {
            showToast(R.string.saved)
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            viewModel.saveShareTag()
        }
    }
}