package com.dinhlam.sharebox.ui.boxform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.UiThread
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.databinding.ActivityBoxFormBinding
import com.dinhlam.sharebox.extensions.doAfterTextChangedDebounce
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
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

    private var isVisiblePasscode: Boolean = false

    override fun onCreateViewBinding(): ActivityBoxFormBinding {
        return ActivityBoxFormBinding.inflate(layoutInflater)
    }

    override val viewModel: BoxFormViewModel by viewModels()

    override fun onStateChanged(state: BoxFormState) {
        if (state.boxDetail != null) {
            binding.checkboxChangePasscode.isVisible = true
            binding.toolbar.title = getString(R.string.title_edit_box)
            binding.textLayoutPasscode.isVisible = state.isChangePasscode
        } else {
            binding.checkboxChangePasscode.isVisible = false
            binding.toolbar.title = getString(R.string.title_create_box)
            binding.textLayoutPasscode.isVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.imageClear.setImageDrawable(Icons.clearIcon(this) {
            copy(sizeDp = 16)
        })

        binding.imageClear.setOnClickListener {
            binding.textEditPasscode.text?.clear()
        }

        binding.checkboxChangePasscode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setChangePasscodeChecked(isChecked)
        }

        binding.textEditPasscode.doAfterTextChangedDebounce(200, lifecycleScope) { text ->
            val takenText = text.trimmedString()

            if (takenText.isBlank()) {
                binding.textLayoutPasscode.endIconDrawable = null
            } else {
                togglePasscodeVisibility()
            }

            binding.imageClear.isVisible = takenText.isNotBlank()
        }

        binding.textLayoutPasscode.setEndIconOnClickListener {
            isVisiblePasscode = !isVisiblePasscode
            togglePasscodeVisibility()
        }

        binding.textEditName.doAfterTextChanged { editable ->
            if (editable.trimmedString().isNotBlank()) {
                binding.textEditName.error = null
            }
        }

        binding.textEditPasscode.setOnClickListener {
            passcodeResultLauncher.launch(router.passcodeIntent(this))
        }

        binding.buttonSave.setOnClickListener {
            onSave()
        }

        viewModel.onChange(this, BoxFormState::boxDetail) { boxDetail ->
            binding.textEditName.setText(boxDetail?.boxName)
            binding.textEditDesc.setText(boxDetail?.boxDesc)
        }

        viewModel.onChange(this, BoxFormState::asyncLoadSave) { asyncLoad ->
            binding.viewLoading.isVisible = asyncLoad.loading
            val box = asyncLoad.data
            box?.let { createdBox ->
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(AppExtras.EXTRA_BOX_ID, createdBox.boxId)
                )
                finish()
            }
        }

        binding.cardContainerMember.isVisible = getState(viewModel, BoxFormState::boxId) != null
        binding.containerMembers.setOnClickListener {
            if (userHelper.isSignedIn()) {
                val boxId = getState(viewModel, BoxFormState::boxId)!!
                startActivity(router.boxMembers(this, boxId))
            } else {
                showToast(R.string.require_sign_in_to_manage_member)
                signInLauncher.launch(router.signIn(true))
            }
        }
    }

    private fun handleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val boxId = getState(viewModel, BoxFormState::boxId)!!
            startActivity(router.boxMembers(this, boxId))
        }
    }

    @UiThread
    private fun togglePasscodeVisibility() {
        if (isVisiblePasscode) {
            binding.textLayoutPasscode.endIconDrawable = Icons.visibilityOnIcon(this)
            binding.textEditPasscode.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
        } else {
            binding.textLayoutPasscode.endIconDrawable = Icons.visibilityOffIcon(this)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}