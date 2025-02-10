package com.dinhlam.sharebox.dialog.optionmenu

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import com.dinhlam.sharebox.base.BaseBottomSheetDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogFragmentBottomSheetOptionsMenuBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getParcelableArrayExtraCompat
import com.dinhlam.sharebox.listmodel.IconTextListModel
import kotlinx.parcelize.Parcelize

class BottomSheetOptionsMenuDialogFragment :
    BaseBottomSheetDialogFragment<DialogFragmentBottomSheetOptionsMenuBinding>() {

    @Parcelize
    data class SingleChoiceItem(
        val icon: String,
        val text: String,
    ) : Parcelable

    companion object {

        @JvmStatic
        fun show(
            fragmentManager: FragmentManager,
            items: Array<BottomSheetOptionsMenuDialogFragment.SingleChoiceItem>,
            args: Bundle = bundleOf(),
            itemSelectedListener: OnOptionItemSelectedListener? = null
        ) {
            BottomSheetOptionsMenuDialogFragment().apply {
                arguments = bundleOf(
                    AppExtras.EXTRA_CHOICE_ITEMS to items
                ).apply { putAll(args) }
                this.itemSelectedListener = itemSelectedListener
            }.show(fragmentManager, "SingleChoiceBottomSheetDialogFragment")
        }
    }

    var itemSelectedListener: OnOptionItemSelectedListener? = null

    fun interface OnOptionItemSelectedListener {
        fun onOptionItemSelected(position: Int, item: String, args: Bundle)
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFragmentBottomSheetOptionsMenuBinding {
        return DialogFragmentBottomSheetOptionsMenuBinding.inflate(inflater, container, false)
    }

    private val choiceItems: Array<SingleChoiceItem> by lazy {
        arguments?.getParcelableArrayExtraCompat(AppExtras.EXTRA_CHOICE_ITEMS) ?: arrayOf()
    }

    private val choiceAdapter = BaseListAdapter.create {
        choiceItems.forEachIndexed { index, choiceItem ->
            IconTextListModel(
                "choice_$index",
                choiceItem.icon,
                choiceItem.text,
                actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                    onItemSelected(index, choiceItem.text)
                })
            ).attachTo(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (choiceItems.isEmpty()) {
            return dismiss()
        }

        binding.root.updateLayoutParams {
            height = choiceItems.size.coerceAtMost(10) * 50.dp()
        }
        binding.recyclerView.adapter = choiceAdapter
        choiceAdapter.requestBuildListModels()
    }

    private fun onItemSelected(position: Int, item: String) {
        itemSelectedListener?.onOptionItemSelected(position, item, Bundle().apply {
            arguments?.let { args -> putAll(args) }
        })
        dismiss()
    }
}