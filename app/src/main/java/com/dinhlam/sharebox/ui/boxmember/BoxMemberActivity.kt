package com.dinhlam.sharebox.ui.boxmember

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityBoxMemberBinding
import com.dinhlam.sharebox.databinding.DialogLayoutInputBinding
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.listmodel.BoxMemberListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BoxMemberActivity :
    BaseViewModelActivity<BoxMemberState, BoxMemberViewModel, ActivityBoxMemberBinding>() {

    private val memberAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.loading) {
                LoadingListModel("loading").attachTo(this)
                return@getState
            }

            state.members.forEach { member ->
                BoxMemberListModel(
                    "member_${member.memberId}",
                    member.memberEmail,
                    onTrashClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        viewModel.removeMember(member)
                    })
                ).attachTo(this)

                VerticalDividerListModel("divider_${member.memberId}").attachTo(this)
            }
        }
    }

    override fun onCreateViewBinding(): ActivityBoxMemberBinding {
        return ActivityBoxMemberBinding.inflate(layoutInflater)
    }

    override val viewModel: BoxMemberViewModel by viewModels()

    override fun onStateChanged(state: BoxMemberState) {
        memberAdapter.requestBuildListModels()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        memberAdapter.attachTo(binding.recyclerView, this)

        binding.imageAdd.setOnClickListener {
            showDialogInputEmail()
        }
    }

    private fun showDialogInputEmail() {
        val binding = DialogLayoutInputBinding.inflate(LayoutInflater.from(this))
        binding.dialogTextInputEdit.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.dialogTextInputEdit.hint = getString(R.string.hint_input_email)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_add_member)
            .setPositiveButton(R.string.add) { _, _ ->
                val email = binding.dialogTextInputEdit.text?.toString() ?: return@setPositiveButton
                if (isValid(email)) {
                    viewModel.addMember(email)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setView(binding.root)
            .show()
    }

    private fun isValid(email: String): Boolean {
        val members = getState(viewModel, BoxMemberState::members)
        if (members.any { boxMember -> boxMember.memberEmail == email }) {
            showToast(R.string.duplicate_member)
            return false
        }
        return true
    }
}