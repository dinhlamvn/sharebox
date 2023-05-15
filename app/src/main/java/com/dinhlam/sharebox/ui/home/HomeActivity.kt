package com.dinhlam.sharebox.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.ui.comment.CommentFragment
import com.dinhlam.sharebox.ui.home.community.CommunityFragment
import com.dinhlam.sharebox.ui.home.profile.ProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private val pageAdapter = object : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 1) ProfileFragment() else CommunityFragment()
        }
    }

    override fun onCreateViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.viewPager.adapter = pageAdapter
        viewBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_community
                    1 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_profile
                }
            }
        })
        viewBinding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val pos = when (menuItem.itemId) {
                R.id.navigation_community -> 0
                else -> 1
            }
            viewBinding.viewPager.setCurrentItem(pos, true)
            return@setOnItemSelectedListener true
        }

        viewBinding.bottomNavigationView.background = null
        viewBinding.bottomNavigationView.menu.getItem(1).isEnabled = false

        viewBinding.buttonCreateFolder.setOnClickListener {
            CommentFragment().apply {
                arguments = Bundle().apply {
                    putString(AppExtras.EXTRA_SHARE_ID, "1234")
                }
            }.show(supportFragmentManager, "CommentFragment")
        }
    }
}
