package com.dinhlam.sharekeeper.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharekeeper.R
import com.dinhlam.sharekeeper.base.BaseFragment
import com.dinhlam.sharekeeper.base.BaseListAdapter
import com.dinhlam.sharekeeper.databinding.FragmentHomeBinding
import com.dinhlam.sharekeeper.ui.home.viewholder.HomeImageViewHolder
import com.dinhlam.sharekeeper.ui.home.viewholder.HomeTextViewHolder

class HomeFragment : BaseFragment<HomeData, HomeViewModel, FragmentHomeBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.adapter = homeAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.reload()
        }

        viewModel.consumeOnChange(HomeData::listItem) { data ->
            homeAdapter.submitList(data)
        }
    }

    override val viewModel: HomeViewModel by viewModels()

    override fun onDataChanged(data: HomeData) {
        viewBinding.swipeRefreshLayout.isRefreshing = data.isRefreshing
    }

    private val homeAdapter =
        BaseListAdapter.createAdapter { layoutRes: Int, view: View ->
            return@createAdapter when (layoutRes) {
                R.layout.text_item_view -> HomeTextViewHolder(view)
                R.layout.image_item_view -> HomeImageViewHolder(view)
                else -> throw NullPointerException("View Holder not found")
            }
        }
}