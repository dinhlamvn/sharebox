package com.dinhlam.sharebox.ui.setting

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.underline
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.databinding.ActivitySettingBinding
import com.dinhlam.sharebox.extensions.coerceMinMax
import com.dinhlam.sharebox.extensions.getColorCompat
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.TransferDataHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.AppSettings
import com.dinhlam.sharebox.pref.UserSharePref
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.services.AppNotificationListenerService
import com.dinhlam.sharebox.services.RealtimeServiceManager
import com.dinhlam.sharebox.utils.AppUtils
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.WorkerUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    @Inject
    lateinit var userSharePref: UserSharePref

    @Inject
    lateinit var transferDataHelper: TransferDataHelper

    @Inject
    lateinit var realtimeServiceManager: RealtimeServiceManager

    @Inject
    lateinit var boxRepository: BoxRepository

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleSignInResult
    )

    private fun handleSignInResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            binding.textAction.setText(R.string.sign_out)
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

        binding.buttonSyncData.setDrawableCompat(Icons.syncIcon(this))
        binding.buttonSyncData.setOnClickListener {
            if (!userHelper.isSignedIn()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.title_alert)
                    .setMessage(R.string.require_sign_to_sync_cloud)
                    .setPositiveButton(R.string.sign_in) { _, _ ->
                        signInLauncher.launch(router.signIn(true))
                    }
                    .setNegativeButton(R.string.alert_no_thanks, null)
                    .show()
            } else {
                WorkerUtils.enqueueJobSyncDataOneTime(this)
            }
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

        binding.textAction.setOnClickListener {
            if (userHelper.isSignedIn()) {
                requestSignOut()
            } else {
                signInLauncher.launch(router.signIn(true))
            }
        }

        binding.textAction.setText(
            if (userHelper.isSignedIn()) {
                R.string.sign_out
            } else {
                R.string.sign_in
            }
        )
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

        binding.seekbarNumOfRecently.progress =
            appSettingHelper.getNumOfRecently()
        binding.textNumOfRecently.text = "${binding.seekbarNumOfRecently.progress}"

        binding.seekbarNumOfRecently.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {

            private var toast: Toast? = null

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && progress < AppConsts.LOADING_LIMIT_ITEM_PER_PAGE) {
                    toast?.cancel()
                    toast = showToast(
                        getString(
                            R.string.require_min_num_recently, AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
                        )
                    )
                    seekBar?.progress = AppConsts.LOADING_LIMIT_ITEM_PER_PAGE
                }
                binding.textNumOfRecently.text = "${seekBar?.progress ?: 0}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress =
                    seekBar?.progress?.coerceMinMax(AppConsts.LOADING_LIMIT_ITEM_PER_PAGE, 100)
                        ?: return
                appSettingHelper.setNumOfRecently(progress)
            }
        })

        binding.switchAutoSync.isChecked = appSettingHelper.isSyncDataInBackground()

        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            appSettingHelper.setSyncDataInBackground(isChecked)
            if (isChecked) {
                WorkerUtils.enqueueJobSyncDataEveryDay(applicationContext)
                showToast(R.string.message_enqueue_sync_data)
            } else {
                WorkerUtils.cancelJobSyncData(applicationContext)
                showToast(R.string.message_cancel_enqueue_sync_data)
            }
        }

        binding.switchAutoSync.isVisible = userHelper.isSignedIn()
        binding.textAbout.text = buildSpannedString {
            underline {
                color(getColorCompat(R.color.md_theme_primary)) {
                    append(
                        getString(
                            R.string.setting_about,
                            getString(R.string.app_name),
                            BuildConfig.VERSION_NAME
                        )
                    )
                }
            }
        }
        binding.textAbout.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=com.dinhlam.sharebox".toUri()
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        binding.switchArchiveNotification.isChecked = appSettingHelper.isRecordingNotifications()
        binding.switchArchiveNotification.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                if (!AppUtils.hasNotificationAccess(this)) {
                    showToast(R.string.archive_notifications_permission_message)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    } else {
                        startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                    button.isChecked = false
                } else {
                    activityScope.launch(Dispatchers.IO) {
                        val box = boxRepository.findOneRaw(userHelper.notificationsBoxId)
                        if (box == null) {
                            boxRepository.insert(
                                "Notifications",
                                id = userHelper.notificationsBoxId,
                                createdBy = userHelper.getCurrentUserId()
                            )
                        }
                        appSettingHelper.setRecordingNotifications(true)
                        startService(
                            Intent(
                                this@SettingActivity,
                                AppNotificationListenerService::class.java
                            ).setAction(AppNotificationListenerService.ACTION_START_SERVICE)
                        )
                    }
                }
            } else {
                appSettingHelper.setRecordingNotifications(false)
                startService(
                    Intent(
                        this@SettingActivity,
                        AppNotificationListenerService::class.java
                    ).setAction(AppNotificationListenerService.ACTION_STOP_SERVICE)
                )
            }
        }
    }

    private fun requestSignOut() {
        MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_confirm)
            .setMessage(R.string.sign_out_confirm_message)
            .setPositiveButton(R.string.sign_out) { _, _ ->
                val currentUserId = userHelper.getCurrentUserId()
                lifecycleScope.launch(Dispatchers.Main) {
                    transferDataHelper.transferData(
                        currentUserId,
                        userSharePref.getAnonymousUserId()
                    )
                    userHelper.signOut(this@SettingActivity, this, {
                        realtimeServiceManager.unbindRealtimeService()
                        binding.textAction.setText(R.string.sign_in)
                        showToast(R.string.logged_out)
                    }, {
                        showToast(R.string.logged_out_error)
                    })
                }
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