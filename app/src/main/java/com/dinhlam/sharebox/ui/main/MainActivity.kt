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
import com.dinhlam.sharebox.ui.discover.DiscoverFragment
import com.dinhlam.sharebox.ui.download.DownloadFragment
import com.dinhlam.sharebox.ui.home.HomeFragment
import com.dinhlam.sharebox.ui.profile.ProfileFragment
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

    class ViewPagerAdapter @Inject constructor(@ActivityContext context: Context) :
        FragmentStateAdapter(context.castNonNull<AppCompatActivity>()) {

        @Inject
        lateinit var homeFragment: HomeFragment

        @Inject
        lateinit var discoverFragment: DiscoverFragment

        @Inject
        lateinit var downloadFragment: DownloadFragment

        @Inject
        lateinit var profileFragment: ProfileFragment

        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> homeFragment
                1 -> discoverFragment
                2 -> downloadFragment
                3 -> profileFragment
                else -> error("No Fragment found for position $position")
            }
        }
    }
}