package com.dinhlam.sharebox.dialog.box

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogBoxSelectionBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.doAfterTextChangedDebounce
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoxSelectionDialogFragment :
    BaseViewModelDialogFragment<BoxSelectionDialogState, BoxSelectionDialogViewModel, DialogBoxSelectionBinding>() {

    fun interface OnBoxSelectedListener {
        fun onBoxSelected(boxId: String)
    }

    @Inject
    lateinit var router: Router

    private var blockVerifyPasscodeBlock: Function0<Unit>? = null

    private val passcodeConfirmResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                blockVerifyPasscodeBlock?.invoke()
            } else {
                showToast(R.string.error_require_passcode)
            }
            blockVerifyPasscodeBlock = null
        }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogBoxSelectionBinding {
        return DialogBoxSelectionBinding.inflate(inflater, container, false)
    }

    private val boxAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isLoading) {
                add(LoadingModelView("loading_box", height = 100.dp()))
                return@getState
            }

            if (state.isSearching) {
                if (state.searchBoxes.isEmpty()) {
                    add(
                        TextModelView(
                            "text_search_result_empty",
                            getString(R.string.search_box_result_empty),
                            height = 100.dp()
                        )
                    )
                } else {
                    state.searchBoxes.forEach { box ->
                        add(TextModelView("text_${box.boxId}",
                            box.boxName,
                            height = 50.dp(),
                            gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                            actionClick = BaseListAdapter.NoHashProp(
                                View.OnClickListener {
                                    onBoxSelected(box.boxId, box.boxName, box.passcode)
                                },
                            ),
                            endIcon = if (box.passcode?.isNotBlank() == true) Icons.lockIcon(
                                requireContext()
                            ) { copy(sizeDp = 16) } else null))
                    }
                }

                return@getState
            }

            state.boxes.forEach { box ->
                add(TextModelView("text_${box.boxId}",
                    box.boxName,
                    height = 50.dp(),
                    gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                    actionClick = BaseListAdapter.NoHashProp(
                        View.OnClickListener {
                            onBoxSelected(box.boxId, box.boxName, box.passcode)
                        },
                    ),
                    endIcon = if (box.passcode?.isNotBlank() == true) Icons.lockIcon(
                        requireContext()
                    ) { copy(sizeDp = 16) } else null))
            }

            if (state.isLoadingMore) {
                add(LoadingModelView("loading_more_${state.currentPage}", height = 50.dp()))
            } else {
                if (state.totalBox > state.boxes.size) {
                    add(
                        TextModelView(
                            "text_total_box",
                            getString(
                                R.string.total_box, state.totalBox - state.boxes.size
                            ),
                            height = 50.dp(), gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                            actionClick = BaseListAdapter.NoHashProp(
                                View.OnClickListener {
                                    viewModel.loadNextPage()
                                },
                            ),
                        )
                    )
                }
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

        viewBinding.editSearch.doAfterTextChangedDebounce(300, lifecycleScope) { editable ->
            viewModel.search(editable.trimmedString())
        }

        viewBinding.buttonGoToGeneral.setOnClickListener {
            returnSelectedBox("")
        }
    }

    override fun getSpacing(): Int {
        return 32
    }

    private fun onBoxSelected(boxId: String, boxName: String, passcode: String?) {
        passcode?.let { boxPasscode ->
            blockVerifyPasscodeBlock = { returnSelectedBox(boxId) }
            val intent = router.passcodeIntent(requireContext(), boxPasscode)
            intent.putExtra(
                AppExtras.EXTRA_PASSCODE_DESCRIPTION, getString(
                    R.string.dialog_bookmark_collection_picker_verify_passcode, boxName
                )
            )
            passcodeConfirmResultLauncher.launch(intent)
        } ?: returnSelectedBox(boxId)
    }

    private fun returnSelectedBox(boxId: String) {
        activity?.cast<OnBoxSelectedListener>()?.onBoxSelected(boxId)
            ?: parentFragment.cast<OnBoxSelectedListener>()?.onBoxSelected(boxId)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        blockVerifyPasscodeBlock = null
    }
}