package com.dinhlam.sharebox.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.services.RealtimeDatabaseService
import com.dinhlam.sharebox.services.ShareCommunityService
import com.dinhlam.sharebox.services.VideoMixerService
import com.dinhlam.sharebox.ui.home.bookmark.BookmarkFragment
import com.dinhlam.sharebox.ui.home.community.CommunityFragment
import com.dinhlam.sharebox.ui.home.profile.ProfileFragment
import com.dinhlam.sharebox.ui.home.videomixer.VideoMixerFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    companion object {
        private const val PAGE_SIZE = 4
    }

    private val pageAdapter = object : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = PAGE_SIZE

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                1 -> VideoMixerFragment()
                2 -> BookmarkFragment()
                3 -> ProfileFragment()
                else -> CommunityFragment()
            }
        }
    }

    private val realtimeDatabaseService by lazy {
        Intent(
            this,
            RealtimeDatabaseService::class.java
        )
    }

    private val communityServiceConnection = object : ServiceConnection {

        var bound = false
            private set

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            bound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            bound = false
        }
    }

    private val videoMixerServiceConnection = object : ServiceConnection {

        var bound = false
            private set

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            bound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            bound = false
        }
    }

    @Inject
    lateinit var appRouter: AppRouter

    override fun onCreateViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextCompat.startForegroundService(this, realtimeDatabaseService)

        viewBinding.viewPager.isUserInputEnabled = false
        viewBinding.viewPager.adapter = pageAdapter

        viewBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_community
                    1 -> viewBinding.bottomNavigationView.selectedItemId =
                        R.id.navigation_video_mixer

                    2 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_bookmark
                    3 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_profile
                }
            }
        })

        viewBinding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val pos = when (menuItem.itemId) {
                R.id.navigation_video_mixer -> 1
                R.id.navigation_bookmark -> 2
                R.id.navigation_profile -> 3
                else -> 0
            }
            viewBinding.viewPager.setCurrentItem(pos, false)
            return@setOnItemSelectedListener true
        }

        viewBinding.bottomNavigationView.menu.getItem(2).isEnabled = false

        viewBinding.buttonAddBox.setOnClickListener {
            startActivity(appRouter.boxIntent(this))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(realtimeDatabaseService)
    }

    override fun onStart() {
        super.onStart()
        Intent(this, ShareCommunityService::class.java).also { intent ->
            bindService(intent, communityServiceConnection, Context.BIND_AUTO_CREATE)
        }
        Intent(this, VideoMixerService::class.java).also { intent ->
            bindService(intent, videoMixerServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (communityServiceConnection.bound) {
            unbindService(communityServiceConnection)
        }
        if (videoMixerServiceConnection.bound) {
            unbindService(videoMixerServiceConnection)
        }
    }
}
