package com.dinhlam.sharebox.ui.box

import android.app.Activity
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
import com.dinhlam.sharebox.databinding.ActivityBoxBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.utils.BoxUtils
import com.dinhlam.sharebox.utils.IconUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BoxActivity : BaseActivity<ActivityBoxBinding>() {

    @Inject
    lateinit var boxRepository: BoxRepository

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var userHelper: UserHelper

    private val passcodeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val passcode = result?.data?.getStringExtra(AppExtras.EXTRA_PASSCODE)
                    ?: return@registerForActivityResult
                viewBinding.textEditPasscode.setText(passcode)
                Logger.debug(passcode)
            }
        }

    private var isVisiblePasscode: Boolean = false

    override fun onCreateViewBinding(): ActivityBoxBinding {
        return ActivityBoxBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.imageClear.setImageDrawable(IconUtils.clearIcon(this) {
            copy(sizeDp = 16)
        })

        viewBinding.imageClear.setOnClickListener {
            viewBinding.textEditPasscode.text?.clear()
        }

        viewBinding.textEditPasscode.doAfterTextChanged { text ->
            val takenText = text.trimmedString()

            if (takenText.isBlank()) {
                viewBinding.textLayoutPasscode.endIconDrawable = null
            } else {
                togglePasscodeVisibility()
            }

            viewBinding.imageClear.isVisible = takenText.isNotBlank()
        }

        viewBinding.textLayoutPasscode.setEndIconOnClickListener {
            isVisiblePasscode = !isVisiblePasscode
            togglePasscodeVisibility()
        }

        viewBinding.textEditName.doAfterTextChanged { editable ->
            if (editable.trimmedString().isNotBlank()) {
                viewBinding.textEditName.error = null
            }
        }

        viewBinding.textEditPasscode.setOnClickListener {
            passcodeResultLauncher.launch(appRouter.passcodeIntent(this))
        }

        viewBinding.buttonCreate.setOnClickListener {
            createNewBox()
        }
    }

    @UiThread
    private fun togglePasscodeVisibility() {
        if (isVisiblePasscode) {
            viewBinding.textLayoutPasscode.endIconDrawable = IconUtils.visibilityOnIcon(this)
            viewBinding.textEditPasscode.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
        } else {
            viewBinding.textLayoutPasscode.endIconDrawable = IconUtils.visibilityOffIcon(this)
            viewBinding.textEditPasscode.transformationMethod =
                PasswordTransformationMethod.getInstance()
        }
    }

    private fun createNewBox() {
        val name = viewBinding.textEditName.getTrimmedText()
        val desc = viewBinding.textEditDesc.getTrimmedText()
        val passcode = viewBinding.textEditPasscode.getTrimmedText()

        if (name.isEmpty()) {
            viewBinding.textEditName.error = getString(R.string.error_require_name)
            return
        }

        activityScope.launch {
            withContext(Dispatchers.Main) {
                viewBinding.viewLoading.show()
            }

            val box = boxRepository.insert(
                BoxUtils.createBoxId(name),
                name,
                desc,
                userHelper.getCurrentUserId(),
                nowUTCTimeInMillis(),
                passcode.takeIfNotNullOrBlank()?.md5()
            )

            if (box == null) {
                withContext(Dispatchers.Main) {
                    showToast(R.string.error_create_box)
                    viewBinding.viewLoading.hide()
                }
            } else {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}