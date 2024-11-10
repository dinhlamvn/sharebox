package com.dinhlam.sharebox.ui.boxlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import com.dinhlam.sharebox.listmodel.BoxListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoxListActivity :
    BaseViewModelActivity<BoxListState, BoxListViewModel, ActivityBoxListBinding>() {

    fun interface OnBoxSelectedListener {
        fun onBoxSelected(boxId: String, boxName: String)
    }

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
                val selectedBox = getState(viewModel, BoxListState::selectedBox)
                    ?: return@registerForActivityResult showToast(R.string.please_choose_box)
                returnSelectedBox(selectedBox)
            } else {
                showToast(R.string.error_require_passcode)
                viewModel.setSelectedBox(null)
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
                        BoxListModel(
                            "box_${boxDetail.boxId}",
                            boxDetail.boxId,
                            boxDetail.boxName,
                            boxDetail.createdDate,
                            Spacing.None,
                            !boxDetail.passcode.isNullOrBlank(),
                            false,
                            NoHashProp(View.OnClickListener {
                                viewModel.setSelectedBox(boxDetail)
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
                BoxListModel(
                    "box_${boxDetail.boxId}",
                    boxDetail.boxId,
                    boxDetail.boxName,
                    boxDetail.createdDate,
                    Spacing.None,
                    !boxDetail.passcode.isNullOrBlank(),
                    false,
                    NoHashProp(View.OnClickListener {
                        viewModel.setSelectedBox(boxDetail)
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
                        actionClick = BaseListAdapter.NoHashProp(
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        boxAdapter.attachTo(binding.recyclerView, this)
        boxAdapter.requestBuildListModels()

        binding.editSearch.doAfterTextChangedDebounce(300, lifecycleScope) { editable ->
            viewModel.search(editable.trimmedString())
        }

        binding.imageAdd.setImageDrawable(Icons.plusIcon(this))

        binding.imageAdd.setOnClickListener {
            createBoxResultLauncher.launch(router.boxForm(this, null))
        }

        viewModel.onChange(BoxListState::selectedBox) { selectedBox ->
            if (selectedBox != null) {
                onBoxSelected(selectedBox)
            }
        }
    }

    private fun onBoxSelected(selectedBox: BoxDetail) = getState(viewModel) { state ->
        if (selectedBox.passcode != null) {
            passcodeConfirmResultLauncher.launch(
                router.passcodeIntent(this, selectedBox.passcode).putExtra(
                    AppExtras.EXTRA_PASSCODE_DESCRIPTION, getString(
                        R.string.dialog_bookmark_collection_picker_verify_passcode,
                        selectedBox.boxName
                    )
                )
            )
        } else {
            returnSelectedBox(selectedBox)
        }
    }

    private fun returnSelectedBox(boxDetail: BoxDetail) {
        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(AppExtras.EXTRA_BOX_ID, boxDetail.boxId)
                .putExtra(AppExtras.EXTRA_BOX_NAME, boxDetail.boxName)
        )
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}