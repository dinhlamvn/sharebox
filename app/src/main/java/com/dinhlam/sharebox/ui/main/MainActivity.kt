package com.dinhlam.sharebox.ui.main

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.databinding.ActivityMainBinding
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.ui.discover.DiscoverFragment
import com.dinhlam.sharebox.ui.download.DownloadFragment
import com.dinhlam.sharebox.ui.home.HomeFragment
import com.dinhlam.sharebox.ui.profile.ProfileFragment
import com.dinhlam.sharebox.utils.LiveEvents
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    @Inject
    lateinit var adapter: ViewPagerAdapter

    override fun onCreateViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressHandler {
            if (binding.bottomNavigationView.selectedItemId != R.id.home) {
                binding.bottomNavigationView.selectedItemId = R.id.home
            } else {
                finish()
            }
        }

        binding.viewPager.adapter = adapter

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val pos = when (menuItem.itemId) {
                R.id.home -> 0
                R.id.discover -> 1
                R.id.download -> 2
                R.id.me -> 3
                else -> error("${menuItem.itemId} is undefined.")
            }
            binding.viewPager.setCurrentItem(pos, false)
            true
        }
        binding.viewPager.isUserInputEnabled = false
    }

    fun moveToDiscover(tab: Int) {
        binding.bottomNavigationView.selectedItemId = R.id.discover
        LiveEvents.changeDiscoverTab(tab)
    }

    class ViewPagerAdapter @Inject constructor(@ActivityContext context: Context) :
        FragmentStateAdapter(context.castNonNull<AppCompatActivity>()) {

        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> DiscoverFragment()
                2 -> DownloadFragment()
                3 -> ProfileFragment()
                else -> error("No Fragment found for position $position")
            }
        }
    }
}