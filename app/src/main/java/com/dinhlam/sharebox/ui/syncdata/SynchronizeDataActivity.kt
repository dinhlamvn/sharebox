package com.dinhlam.sharebox.ui.syncdata

import android.content.Intent
import android.os.Bundle
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.databinding.ActivitySynchronizeDataBinding
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.AppRouter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SynchronizeDataActivity : BaseActivity<ActivitySynchronizeDataBinding>() {

    override fun onCreateViewBinding(): ActivitySynchronizeDataBinding {
        return ActivitySynchronizeDataBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var appRouter: AppRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityScope.launch(Dispatchers.Main) {
            delay(
                if (appSharePref.isFirstAppOpen()) {
                    3000
                } else {
                    1000
                }
            )

            appSharePref.offFirstAppOpen()

            startActivity(
                appRouter.home()
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}