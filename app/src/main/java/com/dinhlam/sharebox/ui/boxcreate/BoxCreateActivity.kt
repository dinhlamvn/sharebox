package com.dinhlam.sharebox.ui.boxcreate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.databinding.ActivityBoxCreateBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.BoxUtils
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BoxCreateActivity : BaseActivity<ActivityBoxCreateBinding>() {

    @Inject
    lateinit var boxRepository: BoxRepository

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

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

    override fun onCreateViewBinding(): ActivityBoxCreateBinding {
        return ActivityBoxCreateBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.imageClear.setImageDrawable(Icons.clearIcon(this) {
            copy(sizeDp = 16)
        })

        binding.imageClear.setOnClickListener {
            binding.textEditPasscode.text?.clear()
        }

        binding.textEditPasscode.doAfterTextChanged { text ->
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

        binding.buttonCreate.setOnClickListener {
            createNewBox()
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

    private fun createNewBox() {
        val name = binding.textEditName.getTrimmedText()
        val desc = binding.textEditDesc.getTrimmedText()
        val passcode = binding.textEditPasscode.getTrimmedText()

        if (name.isEmpty()) {
            binding.textEditName.error = getString(R.string.error_require_name)
            return
        }

        activityScope.launch {
            withContext(Dispatchers.Main) {
                binding.viewLoading.show()
            }

            val box = boxRepository.insert(
                BoxUtils.createBoxId(name),
                name,
                desc,
                userHelper.getCurrentUserId(),
                nowUTCTimeInMillis(),
                passcode.takeIfNotNullOrBlank()?.md5()
            )

            box?.let { createdBox ->
                realtimeDatabaseRepository.push(createdBox)
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(AppExtras.EXTRA_BOX_ID, createdBox.boxId)
                )
                finish()
            } ?: run {
                withContext(Dispatchers.Main) {
                    showToast(R.string.error_create_box)
                    binding.viewLoading.hide()
                }
            }
        }
    }
}