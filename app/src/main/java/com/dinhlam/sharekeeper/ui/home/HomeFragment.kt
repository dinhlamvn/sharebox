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
import com.dinhlam.sharekeeper.modelview.LoadingModelView
import com.dinhlam.sharekeeper.ui.home.modelview.HomeItemModelView
import com.dinhlam.sharekeeper.ui.home.viewholder.HomeImageViewHolder
import com.dinhlam.sharekeeper.ui.home.viewholder.HomeTextViewHolder
import com.dinhlam.sharekeeper.ui.share.ShareData
import com.dinhlam.sharekeeper.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<HomeData, HomeViewModel, FragmentHomeBinding>() {

    @Inject
    lateinit var gson: Gson

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
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override val viewModel: HomeViewModel by viewModels()

    override fun onDataChanged(data: HomeData) {
        if (data.isRefreshing) {
            homeAdapter.buildModelViews {
                add(LoadingModelView)
            }
            return
        }

        homeAdapter.buildModelViews {
            val list = data.shareList
            addAll(list.map { share ->
                val shareInfo =
                    gson.fromJson(share.shareInfo, ShareData.ShareInfo.ShareText::class.java)
                HomeItemModelView.HomeTextModelView(
                    "${share.id}",
                    shareInfo.text.orEmpty(),
                    share.createdAt
                )
            })
        }
    }

    private val homeAdapter =
        BaseListAdapter.createAdapter { layoutRes: Int, view: View ->
            return@createAdapter when (layoutRes) {
                R.layout.model_view_home_share_text -> HomeTextViewHolder(view)
                R.layout.image_item_view -> HomeImageViewHolder(view)
                R.layout.model_view_loading -> LoadingViewHolder(view)
                else -> throw NullPointerException("View Holder not found")
            }
        }
}