package com.dinhlam.sharesaver.ui.home

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseViewModelActivity
import com.dinhlam.sharesaver.databinding.ActivityMainBinding
import com.dinhlam.sharesaver.modelview.LoadingModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeItemModelView
import com.dinhlam.sharesaver.ui.share.ShareData
import com.dinhlam.sharesaver.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseViewModelActivity<HomeData, HomeViewModel, ActivityMainBinding>() {

    @Inject
    lateinit var gson: Gson

    override val viewModel: HomeViewModel by viewModels()

    private val homeAdapter = BaseListAdapter.createAdapter { layoutRes: Int, view: View ->
        return@createAdapter when (layoutRes) {
            R.layout.model_view_home_share_text -> HomeItemModelView.HomeTextModelView.HomeTextViewHolder(
                view
            )
            R.layout.image_item_view -> HomeItemModelView.HomeImageModelView.HomeImageViewHolder(
                view
            )
            R.layout.model_view_loading -> LoadingViewHolder(view)
            else -> null
        }
    }

    override fun onCreateViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding.recyclerView.adapter = homeAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.reload()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }


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
                        "${share.id}", shareInfo.text.orEmpty(), share.createdAt
                    )
                } else {
                    val shareInfo =
                        gson.fromJson(share.shareInfo, ShareData.ShareInfo.ShareImage::class.java)
                    HomeItemModelView.HomeImageModelView(
                        "${share.id}", shareInfo.uri
                    )
                }
            })
        }
    }
}