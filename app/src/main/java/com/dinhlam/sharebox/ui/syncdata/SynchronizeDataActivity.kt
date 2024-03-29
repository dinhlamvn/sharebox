package com.dinhlam.sharebox.ui.syncdata

import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.databinding.ActivitySynchronizeDataBinding
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SynchronizeDataActivity : BaseActivity<ActivitySynchronizeDataBinding>() {

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    @Inject
    lateinit var userHelper: UserHelper

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                goNext()
            } else {
                showAlertDialog()
            }
        }

    override fun onCreateViewBinding(): ActivitySynchronizeDataBinding {
        return ActivitySynchronizeDataBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var appSettingHelper: AppSettingHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.buttonSignIn.setOnClickListener {
            startActivity(
                router.signIn(false)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                goNext()
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                showAlertDialog()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            goNext()
        }
    }

    private fun goNext() {
        activityScope.launch(Dispatchers.IO) {
            try {
                if (appSharePref.isFirstInstall()) {
                    realtimeDatabaseRepository.sync()
                    appSharePref.offFirstInstall()
                    withContext(Dispatchers.Main) {
                        if (userHelper.isSignedIn()) {
                            startActivity(router.home(true))
                        } else {
                            doAfterSynced()
                        }
                    }
                } else if (userHelper.isSignedIn()) {
                    withContext(Dispatchers.Main) {
                        startActivity(router.home(true))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        startActivity(
                            router.signIn(false)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(e.message)
                }
                finishAndRemoveTask()
            }
        }
    }

    private fun doAfterSynced() {
        binding.progressBar.isVisible = false
        binding.imageDone.setImageDrawable(Icons.doneIcon(this))
        binding.textSync.text = getString(R.string.sync_data_done_first_install)
        binding.imageDone.isVisible = true
        binding.buttonSignIn.isVisible = true
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private fun showAlertDialog() {
        AlertDialog.Builder(this).setTitle(R.string.alert_notice)
            .setMessage(R.string.alert_request_post_notification_permission_message)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }.setNegativeButton(R.string.alert_no_thanks) { _, _ ->
                goNext()
            }.create().show()

    }
}