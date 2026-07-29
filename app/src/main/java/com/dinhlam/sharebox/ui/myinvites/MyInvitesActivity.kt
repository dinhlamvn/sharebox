package com.dinhlam.sharebox.ui.myinvites

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityMyInvitesBinding
import com.dinhlam.sharebox.databinding.DialogLayoutInputBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyInvitesActivity :
    BaseViewModelActivity<MyInvitesState, MyInvitesViewModel, ActivityMyInvitesBinding>() {

    @Inject
    lateinit var router: Router

    private val invitedBoxAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.loading) {
                LoadingListModel("loading").attachTo(this)
                return@getState
            }

            state.boxList.forEach { box ->
                TextListModel(
                    "box_${box.boxId}",
                    box.boxName,
                    height = 50.dp(),
                    gravity = Gravity.CENTER_VERTICAL,
                    padding = Spacing.Horizontal(16.dp(), 16.dp()),
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        startActivity(
                            router.myInviteShareListing(
                                this@MyInvitesActivity,
                                box.invitedBy,
                                box.boxId
                            )
                        )
                    })
                ).attachTo(this)
                VerticalDividerListModel("divider_${box.boxId}").attachTo(this)
            }
        }
    }

    override fun onCreateViewBinding(): ActivityMyInvitesBinding {
        return ActivityMyInvitesBinding.inflate(layoutInflater)
    }

    override val viewModel: MyInvitesViewModel by viewModels()

    override fun onStateChanged(state: MyInvitesState) {
        invitedBoxAdapter.requestBuildListModels()
        when (val importBox = state.importBox) {
            is BaseViewModel.AsyncLoad.Success -> {
                viewModel.consumeImportResult()
                showToast(R.string.box_import_success)
                startActivity(router.boxDetail(this, importBox.value.boxId))
            }
            is BaseViewModel.AsyncLoad.Failed -> {
                viewModel.consumeImportResult()
                showToast(R.string.box_import_failed)
            }
            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        invitedBoxAdapter.attachTo(binding.recyclerView, this)

        viewModel.listenDataChangeEvent(this)

        binding.iconImport.setOnClickListener {
            showImportBoxDialog()
        }
    }

    private fun showImportBoxDialog() {
        val inputBinding = DialogLayoutInputBinding.inflate(LayoutInflater.from(this))
        inputBinding.dialogTextInputEdit.inputType = InputType.TYPE_CLASS_TEXT
        inputBinding.dialogTextInputEdit.hint = getString(R.string.input_box_id)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.import_box)
            .setPositiveButton(R.string.download, null)
            .setNegativeButton(R.string.cancel, null)
            .setView(inputBinding.root)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val boxId = inputBinding.dialogTextInputEdit.text.trimmedString()
                    if (boxId.isBlank()) {
                        inputBinding.dialogTextInputLayout.error =
                            getString(R.string.box_id_required)
                        return@setOnClickListener
                    }
                    inputBinding.dialogTextInputLayout.error = null
                    dialog.dismiss()
                    showToast(R.string.box_import_started)
                    viewModel.importBox(boxId)
                }
        }
        dialog.show()
    }
}
