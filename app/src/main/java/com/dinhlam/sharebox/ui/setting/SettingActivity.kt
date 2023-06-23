package com.dinhlam.sharebox.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.data.model.AppSettings
import com.dinhlam.sharebox.databinding.ActivitySettingBinding
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.utils.IconUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    override fun onCreateViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var appSettingHelper: AppSettingHelper

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var appRouter: AppRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (appSettingHelper.getTheme()) {
            AppSettings.Theme.LIGHT -> viewBinding.radioLight.isChecked = true
            AppSettings.Theme.DARK -> viewBinding.radioDark.isChecked = true
            else -> viewBinding.radioAuto.isChecked = true
        }

        when (appSettingHelper.getNetworkCondition()) {
            AppSettings.NetworkCondition.WIFI_CELLULAR_DATA -> viewBinding.radioWifiAndCellular.isChecked =
                true

            else -> viewBinding.radioWifiOnly.isChecked = true
        }

        viewBinding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            requestChangeTheme(checkedId)
        }

        viewBinding.radioGroupNetworkCondition.setOnCheckedChangeListener { _, checkedId ->
            requestChangeNetworkCondition(checkedId)
        }

        viewBinding.imageSignOut.setOnClickListener {
            requestSignOut()
        }

        viewBinding.imageSignOut.setImageDrawable(IconUtils.signOutIcon(this))
        viewBinding.toolbar.navigationIcon = IconUtils.leftArrowIcon(this) {
            copy(sizeDp = 16)
        }
        viewBinding.toolbar.setNavigationOnClickListener {
            finish()
        }

        viewBinding.textAbout.text = getString(
            R.string.setting_about,
            getString(R.string.app_name),
            BuildConfig.VERSION_NAME
        )
    }

    private fun requestSignOut() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_confirm)
            .setMessage(R.string.sign_out_confirm_message)
            .setPositiveButton(R.string.sign_out) { i, b ->
                userHelper.signOut(this, {
                    startActivity(
                        appRouter.signIn()
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }, {
                    showToast(R.string.logged_out_error)
                })
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun requestChangeNetworkCondition(checkedId: Int) {
        val networkCondition = when (checkedId) {
            R.id.radio_wifi_and_cellular -> AppSettings.NetworkCondition.WIFI_CELLULAR_DATA
            else -> AppSettings.NetworkCondition.WIFI_ONLY
        }

        if (networkCondition == appSettingHelper.getNetworkCondition()) {
            return
        }

        appSettingHelper.setNetworkCondition(networkCondition)
    }

    private fun requestChangeTheme(checkedId: Int) {
        val theme = when (checkedId) {
            R.id.radio_light -> AppSettings.Theme.LIGHT
            R.id.radio_dark -> AppSettings.Theme.DARK
            else -> AppSettings.Theme.AUTOMATIC
        }

        if (theme == appSettingHelper.getTheme()) {
            return
        }

        appSettingHelper.setTheme(theme)

        when (theme) {
            AppSettings.Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppSettings.Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}