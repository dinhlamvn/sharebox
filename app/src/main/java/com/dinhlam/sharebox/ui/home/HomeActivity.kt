package com.dinhlam.sharebox.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.ui.home.community.CommunityFragment
import com.dinhlam.sharebox.ui.home.profile.ProfileFragment
import com.dinhlam.sharebox.ui.home.starred.StarredFragment
import com.dinhlam.sharebox.ui.home.videomixer.VideoMixerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private val pageAdapter = object : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                1 -> VideoMixerFragment()
                2 -> StarredFragment()
                3 -> ProfileFragment()
                else -> CommunityFragment()
            }
        }
    }

    override fun onCreateViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.viewPager.isUserInputEnabled = false

        viewBinding.viewPager.adapter = pageAdapter
        viewBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_community
                    1 -> viewBinding.bottomNavigationView.selectedItemId =
                        R.id.navigation_video_mixer

                    2 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_starred
                    3 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_profile
                }
            }
        })
        viewBinding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val pos = when (menuItem.itemId) {
                R.id.navigation_video_mixer -> 1
                R.id.navigation_starred -> 2
                R.id.navigation_profile -> 3
                else -> 0
            }
            viewBinding.viewPager.setCurrentItem(pos, true)
            return@setOnItemSelectedListener true
        }
    }
}
