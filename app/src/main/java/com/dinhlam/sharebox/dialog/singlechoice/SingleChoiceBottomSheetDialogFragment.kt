package com.dinhlam.sharebox.dialog.singlechoice

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseBottomSheetDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogSingleChoiceBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getParcelableArrayExtraCompat
import com.dinhlam.sharebox.modelview.IconTextListModel
import kotlinx.parcelize.Parcelize

class SingleChoiceBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<DialogSingleChoiceBinding>() {

    @Parcelize
    data class SingleChoiceItem(
        val icon: Int,
        val text: String,
    ) : Parcelable

    fun interface OnOptionItemSelectedListener {
        fun onOptionItemSelected(position: Int, item: String, args: Bundle)
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogSingleChoiceBinding {
        return DialogSingleChoiceBinding.inflate(inflater, container, false)
    }

    private val choiceItems: Array<SingleChoiceItem> by lazy {
        arguments?.getParcelableArrayExtraCompat(AppExtras.EXTRA_CHOICE_ITEMS) ?: arrayOf()
    }

    private val choiceAdapter = BaseListAdapter.createAdapter {
        choiceItems.forEachIndexed { index, choiceItem ->
            add(
                IconTextListModel(
                    "choice_$index",
                    choiceItem.icon,
                    choiceItem.text,
                    height = 50.dp(),
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        onItemSelected(index, choiceItem.text)
                    })
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (choiceItems.isEmpty()) {
            return dismiss()
        }

        viewBinding.root.updateLayoutParams {
            height = choiceItems.size.coerceAtMost(6) * 50.dp()
        }
        viewBinding.recyclerView.adapter = choiceAdapter
        choiceAdapter.requestBuildModelViews()
    }

    private fun onItemSelected(position: Int, item: String) {
        val callback = activity.cast<OnOptionItemSelectedListener>()
            ?: parentFragment.cast<OnOptionItemSelectedListener>()
        callback?.onOptionItemSelected(position, item, Bundle().apply {
            arguments?.let { args -> putAll(args) }
        })
        dismiss()
    }
}