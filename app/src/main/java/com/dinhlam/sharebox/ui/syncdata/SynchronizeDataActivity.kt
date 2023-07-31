package com.dinhlam.sharebox.ui.syncdata

import android.os.Bundle
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.databinding.ActivitySynchronizeDataBinding
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SynchronizeDataActivity : BaseActivity<ActivitySynchronizeDataBinding>() {

    @Inject
    lateinit var realtimeDatabaseRepository: RealtimeDatabaseRepository

    override fun onCreateViewBinding(): ActivitySynchronizeDataBinding {
        return ActivitySynchronizeDataBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityScope.launch(Dispatchers.IO) {
            try {
                if (appSharePref.isFirstInstall()) {
                    realtimeDatabaseRepository.sync()
                    WorkerUtils.enqueueJobSyncVideoPeriodic(applicationContext)
                    appSharePref.offFirstInstall()
                }

                withContext(Dispatchers.Main) {
                    startActivity(router.home(true))
                }
            } catch (e: Exception) {
                startActivity(router.home(true))
            }
        }
    }
}