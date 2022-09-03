package com.dinhlam.keepmyshare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.keepmyshare.base.BaseFragment
import com.dinhlam.keepmyshare.base.BaseListAdapter
import com.dinhlam.keepmyshare.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<HomeData, HomeViewModel, FragmentHomeBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.adapter = homeAdapter
    }

    override val viewModel: HomeViewModel by viewModels()

    override fun onDataChanged(data: HomeData) {
        binding.swipeRefreshLayout.isRefreshing = data.isRefreshing
    }

    private val homeAdapter = BaseListAdapter.createAdapter()
}