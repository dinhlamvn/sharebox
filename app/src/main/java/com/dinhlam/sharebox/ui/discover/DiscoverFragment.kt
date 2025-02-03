package com.dinhlam.sharebox.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dinhlam.sharebox.base.BaseFragment
import com.dinhlam.sharebox.databinding.FragmentDiscoverBinding
import com.dinhlam.sharebox.ui.discover.tiktok.TiktokDiscoverFragment
import com.dinhlam.sharebox.ui.discover.zingnews.ZingNewsDiscoverFragment
import com.dinhlam.sharebox.utils.LiveEvents
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DiscoverFragment : BaseFragment<FragmentDiscoverBinding>() {

    @Inject
    lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDiscoverBinding {
        return FragmentDiscoverBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tab, binding.viewPager) { tab, tabPosition ->
            when (tabPosition) {
                0 -> tab.text = "Tiktok"
                1 -> tab.text = "ZingNews"
            }
        }.attach()

        LiveEvents.discoverTabChangeEvent.observe(viewLifecycleOwner) { tab ->
            binding.viewPager.currentItem = tab
        }
    }

    class ViewPagerAdapter @Inject constructor(
        fragment: Fragment
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TiktokDiscoverFragment()
                1 -> ZingNewsDiscoverFragment()
                else -> error("No Fragment found for position $position")
            }
        }
    }
}