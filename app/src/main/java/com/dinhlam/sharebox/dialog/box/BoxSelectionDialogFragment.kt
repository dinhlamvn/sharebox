package com.dinhlam.sharebox.dialog.box

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.databinding.DialogBoxSelectionBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.TextModelView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BoxSelectionDialogFragment :
    BaseViewModelDialogFragment<BoxSelectionDialogState, BoxSelectionDialogViewModel, DialogBoxSelectionBinding>() {

    fun interface OnBoxSelectedListener {
        fun onBoxSelected(boxId: String)
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogBoxSelectionBinding {
        return DialogBoxSelectionBinding.inflate(inflater, container, false)
    }

    private val boxAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isLoading) {
                LoadingModelView("loading_box", height = 100.dp())
                return@getState
            }

            state.boxes.forEach { box ->
                add(
                    TextModelView(
                        "text_${box.boxId}",
                        box.boxName,
                        height = 50.dp(),
                        actionClick = BaseListAdapter.NoHashProp(
                            View.OnClickListener {
                                onBoxSelected(box.boxId)
                            },
                        ),
                    )
                )
            }
        }
    }

    override val viewModel: BoxSelectionDialogViewModel by viewModels()

    override fun onStateChanged(state: BoxSelectionDialogState) {
        boxAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.recyclerView.adapter = boxAdapter
        boxAdapter.requestBuildModelViews()
    }

    override fun getSpacing(): Int {
        return 32
    }

    private fun onBoxSelected(boxId: String) {
        activity?.cast<OnBoxSelectedListener>()?.onBoxSelected(boxId)
            ?: parentFragment.cast<OnBoxSelectedListener>()?.onBoxSelected(boxId)
        dismiss()
    }
}