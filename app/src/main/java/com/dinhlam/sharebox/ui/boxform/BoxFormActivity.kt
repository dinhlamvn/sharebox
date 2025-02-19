package com.dinhlam.sharebox.ui.boxform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.UiThread
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.databinding.ActivityBoxFormBinding
import com.dinhlam.sharebox.extensions.doAfterTextChangedDebounce
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoxFormActivity :
    BaseViewModelActivity<BoxFormState, BoxFormViewModel, ActivityBoxFormBinding>() {

    @Inject
    lateinit var boxRepository: BoxRepository

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleSignInResult
    )

    private val passcodeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val passcode = result?.data?.getStringExtra(AppExtras.EXTRA_PASSCODE)
                    ?: return@registerForActivityResult
                binding.textEditPasscode.setText(passcode)
                Logger.debug(passcode)
            }
        }

    override fun onCreateViewBinding(): ActivityBoxFormBinding {
        return ActivityBoxFormBinding.inflate(layoutInflater)
    }

    override val viewModel: BoxFormViewModel by viewModels()

    override fun onStateChanged(state: BoxFormState) {
        binding.containerPasscode.isVisible = state.isUsePasscode
        togglePasscodeVisibility(state.isPasscodeVisible)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        binding.checkboxUsePasscode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleUsePasscode(isChecked)
        }

        binding.iconEye.setOnClickListener {
            viewModel.togglePasscodeVisibility()
        }

        binding.textEditName.doAfterTextChangedDebounce(scope = lifecycleScope) { editable ->
            binding.textEditName.error =
                (editable?.toString() != null).ifTrue(null, binding.textEditName.error)
        }

        binding.textEditPasscode.setOnClickListener {
            passcodeResultLauncher.launch(router.passcodeIntent(this))
        }

        binding.iconSave.setOnClickListener {
            onSave()
        }

        onChange(BoxFormState::currentBoxDetail) { boxDetail ->
            binding.textEditName.setText(boxDetail?.boxName)
            binding.textEditDesc.setText(boxDetail?.boxDesc)
            binding.toolbar.title = (boxDetail == null).ifTrue(
                getString(R.string.title_create_box),
                getString(R.string.title_edit_box)
            )
            binding.textUpdatePasscodeDesc.isVisible = boxDetail?.isHasPasscode == true
            binding.cardContainerMember.isVisible = boxDetail != null
        }

        onChange(BoxFormState::asyncLoadSave) { asyncLoad ->
            binding.viewLoading.isVisible = asyncLoad is BaseViewModel.AsyncLoad.Loading
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(AppExtras.EXTRA_BOX_ID, asyncLoad.value.boxId)
                )
                finish()
            } else if (asyncLoad is BaseViewModel.AsyncLoad.Failed) {
                showToast(asyncLoad.error.message)
            }
        }

        binding.containerMembers.setOnClickListener {
            if (userHelper.isSignedIn()) {
                manageBoxMembers()
            } else {
                showToast(R.string.require_sign_in_to_manage_member)
                signInLauncher.launch(router.signIn(true))
            }
        }
    }

    private fun handleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            manageBoxMembers()
        }
    }

    private fun manageBoxMembers() {
        val boxId = getState(viewModel, BoxFormState::currentBoxDetail)?.boxId
            ?: return showToast(R.string.no_box_selected)
        startActivity(router.boxMembers(this, boxId))
    }

    @UiThread
    private fun togglePasscodeVisibility(isVisiblePasscode: Boolean) {
        binding.iconEye.setIconCode(isVisiblePasscode.ifTrue("f06e", "f070"))
        if (isVisiblePasscode) {
            binding.textEditPasscode.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
        } else {
            binding.textEditPasscode.transformationMethod =
                PasswordTransformationMethod.getInstance()
        }
    }

    private fun onSave() {
        val name = binding.textEditName.getTrimmedText()
        val desc = binding.textEditDesc.getTrimmedText()
        val passcode = binding.textEditPasscode.getTrimmedText()

        if (name.isEmpty()) {
            binding.textEditName.error = getString(R.string.error_require_name)
            return
        }

        viewModel.saveBox(name, desc, passcode)
    }
}