package com.dinhlam.sharebox.ui.checklist

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseListAdapter.NoHashProp
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityCheckListBinding
import com.dinhlam.sharebox.databinding.DialogLayoutInputBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.listmodel.ButtonListModel
import com.dinhlam.sharebox.listmodel.CheckListListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.checklist.dialog.CheckListDataFormDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CheckListActivity :
    BaseViewModelActivity<CheckListState, CheckListViewModel, ActivityCheckListBinding>(),
    CheckListDataFormDialogFragment.OnSaveCheckListListener {

    override val viewModel: CheckListViewModel by viewModels()

    private val chooseBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult showToast(
                    R.string.require_choose_box
                )
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID)
                        ?: return@registerForActivityResult showToast(
                            R.string.require_choose_box
                        )
                viewModel.setCurrentBoxId(boxId)
            }
        }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { boxId ->
                    viewModel.setCurrentBoxId(boxId)
                }
            }
        }

    private val checkListAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            state.checkListDataList.forEachIndexed { idx, checkListData ->
                CheckListListModel(
                    "checkList_$idx",
                    checkListData.title,
                    checkListData.done,
                    checkListData.datetime,
                    checkListData.reminder,
                    NoHashProp(View.OnClickListener {
                        showCheckListDataForm(checkListData)
                    })
                ).attachTo(this)

                VerticalDividerListModel("divider_$idx").attachTo(this)
            }

            ButtonListModel(
                "button_create_checklist",
                "+",
                margin = Spacing.Only(16.dp(), 16.dp(), 16.dp(), 0),
                onClick = NoHashProp(View.OnClickListener {
                    showCheckListDataForm(null)
                })
            ).attachTo(
                this
            )
        }
    }

    private fun showCheckListDataForm(checkListData: ShareData.ShareCheckList.CheckListData?) {
        CheckListDataFormDialogFragment()
            .apply {
                arguments = bundleOf(AppExtras.EXTRA_DATA to checkListData)
                saveCheckListListener = this@CheckListActivity
            }
            .show(
                supportFragmentManager,
                "dialog-check-list-form"
            )
    }

    @Inject
    lateinit var router: Router

    override fun onStateChanged(state: CheckListState) {
        binding.loading.toggle(state.asyncArchive is BaseViewModel.AsyncLoad.Loading)
        checkListAdapter.requestBuildListModels()
    }

    override fun onCreateViewBinding(): ActivityCheckListBinding {
        return ActivityCheckListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkListAdapter.attachTo(binding.recyclerView, this)

        binding.iconDone.setOnClickListener {
            getState(viewModel) { state ->
                if (state.checkListDataList.isEmpty()) {
                    showToast(R.string.nothing_to_save)
                    return@getState
                }
                if (state.currentBox == null) {
                    showToast(R.string.please_choose_box)
                    binding.boxSectionButton.playZoomAnimation()
                    return@getState
                }
                showDialogInputTitle(state.shareDetail?.shareNote)
            }
        }

        binding.boxSectionButton.setOnClickListener {
            chooseBoxLauncher.launch(router.boxList(this, null))
        }

        binding.boxSectionButton.setOnAddIconClickListener {
            createBoxResultLauncher.launch(router.boxForm(this, null))
        }

        onChange(CheckListState::currentBox) { currentBox ->
            val boxName = currentBox?.boxName
            val isLock = currentBox?.passcode?.isNotBlank() ?: false
            binding.boxSectionButton.setBoxName(boxName)
            binding.boxSectionButton.showLock(isLock)
        }

        onChange(CheckListState::asyncArchive) { asyncLoad ->
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                showToast(
                    getString(
                        R.string.archive_url_success,
                        asyncLoad.data?.shareNote.orEmpty()
                    )
                )
                finish()
            } else if (asyncLoad is BaseViewModel.AsyncLoad.Failed) {
                showToast(asyncLoad.error.message)
            }
        }
    }

    private fun showDialogInputTitle(title: String?) {
        val binding = DialogLayoutInputBinding.inflate(LayoutInflater.from(this))
        binding.dialogTextInputEdit.inputType = InputType.TYPE_CLASS_TEXT
        binding.dialogTextInputEdit.hint = getString(R.string.text_input_note_hint_desc)
        binding.dialogTextInputEdit.setText(title)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.share_note)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                val newTitle = binding.dialogTextInputEdit.text?.trimmedString()
                viewModel.saveCheckList(newTitle)
            }
            .setNegativeButton(R.string.cancel, null)
            .setView(binding.root)
            .show()
    }

    override fun onSaveCheckListData(
        checkListData: ShareData.ShareCheckList.CheckListData,
        params: Bundle
    ) {
        val oldCheckList =
            params.getParcelableExtraCompat<ShareData.ShareCheckList.CheckListData>(AppExtras.EXTRA_DATA)
        viewModel.saveCheckListData(oldCheckList, checkListData)
    }
}