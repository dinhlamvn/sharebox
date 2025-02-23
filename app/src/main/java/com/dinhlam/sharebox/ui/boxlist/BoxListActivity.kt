package com.dinhlam.sharebox.ui.boxlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseListAdapter.NoHashProp
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityBoxListBinding
import com.dinhlam.sharebox.extensions.doAfterTextChangedDebounce
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.listmodel.BoxItemListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoxListActivity :
    BaseViewModelActivity<BoxListState, BoxListViewModel, ActivityBoxListBinding>() {

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.reload()
            }
        }

    @Inject
    lateinit var router: Router

    private val passcodeConfirmResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                val boxName = data.getStringExtra(AppExtras.EXTRA_BOX_NAME)
                    ?: return@registerForActivityResult
                returnSelectedBox(boxId, boxName)
            } else {
                showToast(R.string.error_require_passcode)
            }
        }

    override fun onCreateViewBinding(): ActivityBoxListBinding {
        return ActivityBoxListBinding.inflate(layoutInflater)
    }

    private val boxAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.isSearching) {
                if (state.searchBoxes.isEmpty()) {
                    TextListModel(
                        "text_search_result_empty",
                        getString(R.string.search_box_result_empty),
                        height = 100.dp()
                    ).attachTo(this)
                } else {
                    state.searchBoxes.forEachIndexed { idx, boxDetail ->
                        BoxItemListModel(
                            "box_${boxDetail.boxId}",
                            boxDetail.boxId,
                            boxDetail.boxName,
                            boxDetail.createdDate,
                            Spacing.None,
                            !boxDetail.passcode.isNullOrBlank(),
                            false,
                            NoHashProp(View.OnClickListener {
                                onBoxSelected(boxDetail)
                            }),
                        ).attachTo(this)

                        VerticalDividerListModel(
                            "box_divider_$idx"
                        ).attachTo(this)
                    }
                }

                return@getState
            }

            state.boxes.forEachIndexed { idx, boxDetail ->
                BoxItemListModel(
                    "box_${boxDetail.boxId}",
                    boxDetail.boxId,
                    boxDetail.boxName,
                    boxDetail.createdDate,
                    Spacing.None,
                    !boxDetail.passcode.isNullOrBlank(),
                    false,
                    NoHashProp(View.OnClickListener {
                        onBoxSelected(boxDetail)
                    }),
                ).attachTo(this)

                VerticalDividerListModel(
                    "box_divider_$idx"
                ).attachTo(this)
            }

            if (state.asyncLoadBoxes is BaseViewModel.AsyncLoad.Loading) {
                LoadingListModel("loading_more_${state.currentPage}", height = 50.dp()).attachTo(
                    this
                )
            } else {
                if (state.totalBox > state.boxes.size) {
                    TextListModel(
                        "text_total_box",
                        getString(
                            R.string.total_box, state.totalBox - state.boxes.size
                        ),
                        height = 50.dp(), gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                        actionClick = NoHashProp(
                            View.OnClickListener {
                                viewModel.loadNextPage()
                            },
                        ),
                    ).attachTo(this)
                }
            }
        }
    }

    override val viewModel: BoxListViewModel by viewModels()

    override fun onStateChanged(state: BoxListState) {
        boxAdapter.requestBuildListModels()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title =
            intent.getStringExtra(AppExtras.EXTRA_TITLE) ?: getString(R.string.choose_box)

        boxAdapter.attachTo(binding.recyclerView, this)
        boxAdapter.requestBuildListModels()

        binding.editSearch.doAfterTextChangedDebounce(300, lifecycleScope) { editable ->
            viewModel.search(editable.trimmedString())
        }

        binding.buttonAdd.setOnClickListener {
            createBoxResultLauncher.launch(router.boxForm(this, null))
        }
    }

    private fun onBoxSelected(selectedBox: BoxDetail) {
        if (selectedBox.passcode != null) {
            passcodeConfirmResultLauncher.launch(
                router.passcodeIntent(
                    this, selectedBox.passcode,
                    bundleOf(
                        AppExtras.EXTRA_BOX_ID to selectedBox.boxId,
                        AppExtras.EXTRA_BOX_NAME to selectedBox.boxName
                    ),
                    getString(
                        R.string.dialog_bookmark_collection_picker_verify_passcode,
                        selectedBox.boxName
                    )
                )
            )
        } else {
            returnSelectedBox(selectedBox.boxId, selectedBox.boxName)
        }
    }

    private fun returnSelectedBox(boxId: String, boxName: String) {
        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(AppExtras.EXTRA_BOX_ID, boxId)
                .putExtra(AppExtras.EXTRA_BOX_NAME, boxName)
        )
        finish()
    }
}