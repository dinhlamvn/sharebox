package com.dinhlam.sharesaver.ui.home

import android.os.Build
import android.os.Bundle
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseViewModelActivity
import com.dinhlam.sharesaver.databinding.ActivityMainBinding
import com.dinhlam.sharesaver.modelview.LoadingModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeFolderModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeImageModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeTextModelView
import com.dinhlam.sharesaver.ui.share.ShareData
import com.dinhlam.sharesaver.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseViewModelActivity<HomeData, HomeViewModel, ActivityMainBinding>() {

    private val modelViewsFactory = object : BaseListAdapter.ModelViewsFactory() {

        override fun buildModelViews() = withData(viewModel) { data ->
            if (data.isRefreshing) {
                LoadingModelView.attachTo(this)
                return@withData
            }

            if (data.shareList.isEmpty()) {
                data.folders.map { folder ->
                    HomeFolderModelView("folder_${folder.id}", folder.name)
                }.forEach { it.attachTo(this) }
            }
            data.shareList.map { share ->
                if (share.shareType == "text") {
                    val shareInfo =
                        gson.fromJson(share.shareInfo, ShareData.ShareInfo.ShareText::class.java)
                    HomeTextModelView(
                        "${share.id}", shareInfo.text.orEmpty(), share.createdAt, share.shareNote
                    )
                } else {
                    val shareInfo =
                        gson.fromJson(share.shareInfo, ShareData.ShareInfo.ShareImage::class.java)
                    HomeImageModelView(
                        "${share.id}", shareInfo.uri, share.createdAt, share.shareNote
                    )
                }
            }.forEach { it.attachTo(this) }
        }
    }

    companion object {
        private const val SPAN_COUNT = 3
    }

    @Inject
    lateinit var gson: Gson

    private val folderLayoutManager by lazy { GridLayoutManager(this, SPAN_COUNT) }

    private val itemLayoutManager by lazy { LinearLayoutManager(this) }

    override val viewModel: HomeViewModel by viewModels()

    private val homeAdapter = BaseListAdapter.createAdapter { layoutRes: Int, view: View ->
        return@createAdapter when (layoutRes) {
            R.layout.model_view_home_share_text -> HomeTextModelView.HomeTextViewHolder(
                view
            )
            R.layout.model_view_home_share_image -> HomeImageModelView.HomeImageViewHolder(
                view
            )
            R.layout.model_view_loading -> LoadingViewHolder(view)
            R.layout.model_view_home_folder -> HomeFolderModelView.HomeFolderViewHolder(view) { position ->
                viewModel.onFolderClick(position)
            }
            else -> null
        }
    }

    override fun onCreateViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                if (viewModel.handleBackPressed()) {
                    return@registerOnBackInvokedCallback
                }
                finish()
            }
        } else {
            onBackPressedDispatcher.addCallback {
                if (viewModel.handleBackPressed()) {
                    return@addCallback
                }
                finish()
            }
        }

        folderLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val model = homeAdapter.getModelAtPosition(position) ?: return 1
                if (model is LoadingModelView) {
                    return SPAN_COUNT
                }
                return 1
            }
        }

        viewBinding.recyclerView.layoutManager = folderLayoutManager
        viewBinding.recyclerView.adapter = homeAdapter
        modelViewsFactory.attach(homeAdapter)

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.reload()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDataChanged(data: HomeData) {
        viewBinding.recyclerView.layoutManager = if (data.shareList.isEmpty()) {
            folderLayoutManager
        } else {
            itemLayoutManager
        }
        modelViewsFactory.requestBuildModelViews()
    }
}