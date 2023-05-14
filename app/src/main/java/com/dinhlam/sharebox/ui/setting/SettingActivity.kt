package com.dinhlam.sharebox.ui.setting

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.data.model.SortType
import com.dinhlam.sharebox.databinding.ActivitySettingBinding
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.pref.AppSharePref
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    @Inject
    lateinit var appSharePref: AppSharePref

    override fun onCreateViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressHandler {
            finish()
        }

        when (appSharePref.getSortType()) {
            SortType.NEWEST -> viewBinding.radioButtonSortByNewest.isChecked = true
            SortType.OLDEST -> viewBinding.radioButtonSortByOldest.isChecked = true
            else -> viewBinding.radioButtonNoSort.isChecked = true
        }

        viewBinding.radioButtonGroup.setOnCheckedChangeListener { _, buttonId ->
            val sortType = when (buttonId) {
                R.id.radio_button_sort_by_newest -> SortType.NEWEST
                R.id.radio_button_sort_by_oldest -> SortType.OLDEST
                else -> SortType.NONE
            }
            appSharePref.setSortType(sortType)
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("sort-type", sortType)
            })
        }

        val isCustomTabEnabled = appSharePref.isCustomTabEnabled()
        viewBinding.switchCustomTab.isChecked = isCustomTabEnabled

        viewBinding.switchCustomTab.setOnCheckedChangeListener { _, isChecked ->
            appSharePref.toggleCustomTab(isChecked)
        }

        val recoveryPassword = appSharePref.getRecoveryPassword()

        if (recoveryPassword.isNotBlank()) {
            viewBinding.textViewPasswordRecoveryTitle.text = "***-***-***"
            viewBinding.textViewPasswordRecoveryDesc.setText(R.string.password_recovery_desc_generated)
            viewBinding.textViewPasswordRecoveryTitle.setDrawableCompat(end = R.drawable.ic_backup_on)
        }

        viewBinding.textViewPasswordRecoveryTitle.setOnClickListener {
            if (recoveryPassword.isNotBlank()) {
                showToast(R.string.password_recovery_has_generated_error)
            } else {
                generateRecoveryPasswordHash()
            }
        }

        val isShowRecently = appSharePref.isShowRecentlyShare()
        viewBinding.switchShowRecently.setOnCheckedChangeListener { _, isOn ->
            if (isOn != appSharePref.isShowRecentlyShare()) {
                setResult(Activity.RESULT_OK)
                appSharePref.toggleShowRecentlyShare(isOn)
            }
        }
        viewBinding.switchShowRecently.isChecked = isShowRecently

        val isFastSave = appSharePref.isFastSave()
        viewBinding.switchFastSave.isChecked = isFastSave
        viewBinding.switchFastSave.setOnCheckedChangeListener { _, isOn ->
            appSharePref.toggleFastSave(isOn)
            if (isOn) {
                showDialogAlertFastSaveEnable()
            }
        }
    }

    private fun showDialogAlertFastSaveEnable() {
        AlertDialog.Builder(this)
            .setMessage(R.string.fast_save_desc)
            .setCancelable(true)
            .setPositiveButton(R.string.button_done, null)
            .show()
    }

    private fun generateRecoveryPasswordHash() {
        val uuid = UUID.randomUUID()
        val recoveryHash = uuid.toString()

        viewBinding.textViewPasswordRecoveryTitle.setDrawableCompat(end = R.drawable.ic_copy)
        viewBinding.textViewPasswordRecoveryTitle.text = buildSpannedString {
            bold {
                append(recoveryHash)
            }
        }
        viewBinding.textViewPasswordRecoveryDesc.setText(R.string.password_recovery_desc_generated)
        viewBinding.textViewPasswordRecoveryTitle.setOnClickListener {
            val clipboardManager =
                getSystemServiceCompat<ClipboardManager>(Context.CLIPBOARD_SERVICE)
            clipboardManager.setPrimaryClip(
                ClipData.newPlainText(
                    "share_box_recovery_password",
                    recoveryHash
                )
            )
            appSharePref.setRecoveryPassword(recoveryHash)
            showToast(R.string.copied)
        }
        appSharePref.setRecoveryPassword(recoveryHash)
        showToast(R.string.password_recovery_generated, duration = Toast.LENGTH_LONG)
        viewBinding.textViewNotification.isVisible = true
    }
}
