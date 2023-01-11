package com.dinhlam.sharebox.dialog.singlechoose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.DialogSingleChoiceBinding
import com.dinhlam.sharebox.modelview.SingleChoiceModelView

class SingleChoiceDialogFragment : BaseBottomSheetDialogFragment<DialogSingleChoiceBinding>() {

    fun interface OnItemSelectedListener {
        fun onItemSelected(position: Int)
    }

    companion object {
        const val EXTRA_ITEM = "extra-items"
    }

    private val items by lazy { arguments?.getStringArray(EXTRA_ITEM) ?: emptyArray() }

    var listener: OnItemSelectedListener? = null

    private val adapter by lazy {
        BaseListAdapter.createAdapter({
            items.map { choice ->
                SingleChoiceModelView("choice_$choice", choice)
            }
        }) {

            withViewType(R.layout.model_view_single_choice) {
                SingleChoiceModelView.SingleChoiceViewHolder(this) { position ->
                    dismiss()
                    listener?.onItemSelected(position)
                }
            }
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogSingleChoiceBinding {
        return DialogSingleChoiceBinding.inflate(inflater, container, false)
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.adapter = adapter
        adapter.requestBuildModelViews()
    }
}