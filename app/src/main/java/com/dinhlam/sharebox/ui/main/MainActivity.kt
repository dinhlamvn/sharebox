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
import com.dinhlam.sharebox.ui.home.HomeFragment
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
                R.id.explore -> 1
                R.id.download -> 3
                R.id.me -> 4
                else -> error("No ID ${menuItem.itemId}")
            }
            binding.viewPager.currentItem = pos
            true
        }
    }

    class ViewPagerAdapter @Inject constructor(@ActivityContext context: Context) :
        FragmentStateAdapter(context.castNonNull<AppCompatActivity>()) {

        @Inject
        lateinit var homeFragment: HomeFragment

        override fun getItemCount(): Int {
            return 1
        }

        override fun createFragment(position: Int): Fragment {
            return homeFragment
        }
    }
}