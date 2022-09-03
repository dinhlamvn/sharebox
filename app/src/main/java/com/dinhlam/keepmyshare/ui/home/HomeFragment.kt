package com.dinhlam.keepmyshare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.keepmyshare.R
import com.dinhlam.keepmyshare.base.BaseFragment
import com.dinhlam.keepmyshare.base.BaseListAdapter
import com.dinhlam.keepmyshare.databinding.FragmentHomeBinding
import com.dinhlam.keepmyshare.ui.home.viewholder.HomeImageViewHolder
import com.dinhlam.keepmyshare.ui.home.viewholder.HomeTextViewHolder

class HomeFragment : BaseFragment<HomeData, HomeViewModel, FragmentHomeBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.adapter = homeAdapter

        viewModel.reload()

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.reload()
        }
    }

    override val viewModel: HomeViewModel by viewModels()

    override fun onDataChanged(data: HomeData) {
        viewBinding.swipeRefreshLayout.isRefreshing = data.isRefreshing
        homeAdapter.submitList(data.listItem)
    }

    private val homeAdapter =
        BaseListAdapter.createAdapter { layoutRes: Int, inflater: LayoutInflater, container: ViewGroup? ->
            val view = inflater.inflate(layoutRes, container, false)
            return@createAdapter when (layoutRes) {
                R.layout.text_item_view -> HomeTextViewHolder(view)
                R.layout.image_item_view -> HomeImageViewHolder(view)
                else -> throw NullPointerException("View Holder not found")
            }
        }
}