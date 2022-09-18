package com.dinhlam.sharesaver.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseFragment
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.FragmentHomeBinding
import com.dinhlam.sharesaver.modelview.LoadingModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeItemModelView
import com.dinhlam.sharesaver.ui.home.viewholder.HomeImageViewHolder
import com.dinhlam.sharesaver.ui.home.viewholder.HomeTextViewHolder
import com.dinhlam.sharesaver.ui.share.ShareData
import com.dinhlam.sharesaver.viewholder.LoadingViewHolder
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
                if (share.shareType == "text") {
                    val shareInfo =
                        gson.fromJson(share.shareInfo, ShareData.ShareInfo.ShareText::class.java)
                    HomeItemModelView.HomeTextModelView(
                        "${share.id}",
                        shareInfo.text.orEmpty(),
                        share.createdAt
                    )
                } else {
                    val shareInfo =
                        gson.fromJson(share.shareInfo, ShareData.ShareInfo.ShareImage::class.java)
                    HomeItemModelView.HomeImageModelView(
                        "${share.id}",
                        shareInfo.uri
                    )
                }
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