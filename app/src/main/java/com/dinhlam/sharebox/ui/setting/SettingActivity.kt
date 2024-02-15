package com.dinhlam.sharebox.ui.setting

import android.app.Activity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.databinding.ActivitySettingBinding
import com.dinhlam.sharebox.extensions.coerceMinMax
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.AppSettings
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.WorkerUtils
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
    lateinit var router: Router

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleSignInResult
    )

    private fun handleSignInResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            binding.imageAction.setImageDrawable(Icons.signOutIcon(this))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressHandler {
            if (isTaskRoot) {
                startActivity(router.home(true))
            } else {
                finish()
            }
        }

        binding.buttonSyncToCloud.setDrawableCompat(Icons.syncIcon(this))
        binding.buttonSyncToCloud.setOnClickListener {
            WorkerUtils.enqueueJobSyncDataOneTime(this)
        }

        when (appSettingHelper.getTheme()) {
            AppSettings.Theme.LIGHT -> binding.radioLight.isChecked = true
            AppSettings.Theme.DARK -> binding.radioDark.isChecked = true
            else -> binding.radioAuto.isChecked = true
        }

        when (appSettingHelper.getNetworkCondition()) {
            AppSettings.NetworkCondition.WIFI_CELLULAR_DATA -> binding.radioWifiAndCellular.isChecked =
                true

            else -> binding.radioWifiOnly.isChecked = true
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            requestChangeTheme(checkedId)
        }

        binding.radioGroupNetworkCondition.setOnCheckedChangeListener { _, checkedId ->
            requestChangeNetworkCondition(checkedId)
        }

        binding.imageAction.setOnClickListener {
            if (userHelper.isSignedIn()) {
                requestSignOut()
            } else {
                signInLauncher.launch(router.signIn(true))
            }
        }

        binding.imageAction.isVisible = userHelper.isSignedIn()
        binding.imageAction.setImageDrawable(Icons.signOutIcon(this))
        binding.toolbar.navigationIcon = Icons.leftArrowIcon(this) {
            copy(sizeDp = 16)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.seekbarImageDownloadQuality.progress =
            appSettingHelper.getImageDownloadQuality()
        binding.textQuality.text = "${binding.seekbarImageDownloadQuality.progress}"


        binding.seekbarImageDownloadQuality.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {

            private var toast: Toast? = null

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && progress < AppConsts.MIN_IMAGE_QUALITY) {
                    toast?.cancel()
                    toast = showToast(
                        getString(
                            R.string.require_min_image_quality, AppConsts.MIN_IMAGE_QUALITY
                        )
                    )
                    seekBar?.progress = AppConsts.MIN_IMAGE_QUALITY
                }
                binding.textQuality.text = "${seekBar?.progress ?: 0}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress =
                    seekBar?.progress?.coerceMinMax(AppConsts.MIN_IMAGE_QUALITY, 100) ?: return
                appSettingHelper.setImageDownloadQuality(progress)
            }
        })

        binding.checkboxSyncInBackground.isChecked = appSettingHelper.isSyncDataInBackground()

        binding.checkboxSyncInBackground.setOnCheckedChangeListener { _, isChecked ->
            appSettingHelper.setSyncDataInBackground(isChecked)
            if (isChecked) {
                WorkerUtils.enqueueJobSyncData(applicationContext)
                showToast(R.string.message_enqueue_sync_data)
            } else {
                WorkerUtils.cancelJobSyncData(applicationContext)
                showToast(R.string.message_cancel_enqueue_sync_data)
            }
        }

        binding.textAbout.text = getString(
            R.string.setting_about, getString(R.string.app_name), BuildConfig.VERSION_NAME
        )
    }

    private fun requestSignOut() {
        MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_confirm)
            .setMessage(R.string.sign_out_confirm_message)
            .setPositiveButton(R.string.sign_out) { _, _ ->
                userHelper.signOut(this, {
                    startActivity(router.home(true))
                }, {
                    showToast(R.string.logged_out_error)
                })
            }.setNegativeButton(R.string.dialog_cancel, null).show()
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