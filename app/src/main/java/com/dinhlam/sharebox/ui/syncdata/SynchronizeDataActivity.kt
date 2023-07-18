package com.dinhlam.sharebox.ui.syncdata

import android.os.Bundle
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.databinding.ActivitySynchronizeDataBinding
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.AppRouter
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
    lateinit var appRouter: AppRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityScope.launch(Dispatchers.IO) {
            realtimeDatabaseRepository.sync()
            WorkerUtils.enqueueJobSyncVideosOneTime(applicationContext)
            withContext(Dispatchers.Main) {
                startActivity(appRouter.home(true))
            }
        }
    }
}