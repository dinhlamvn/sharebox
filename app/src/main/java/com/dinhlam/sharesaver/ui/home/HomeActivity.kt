package com.dinhlam.sharesaver.ui.home

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseViewModelActivity
import com.dinhlam.sharesaver.databinding.ActivityMainBinding
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.extensions.registerOnBackPressHandler
import com.dinhlam.sharesaver.modelview.LoadingModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeDateModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeFolderModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeImageModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeWebLinkModelView
import com.dinhlam.sharesaver.ui.share.ShareData
import com.dinhlam.sharesaver.utils.IconUtils
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

            if (data.selectedFolder == null) {
                data.folders.map { folder ->
                    HomeFolderModelView("folder_${folder.id}", folder.name, folder.desc)
                }.forEach { it.attachTo(this) }
                return@withData
            }
            data.shareList.groupBy { it.createdAt.format("yyyy-MM-dd") }.forEach { entry ->
                val date = entry.key
                val shares = entry.value
                HomeDateModelView("date$date", date).attachTo(this)
                shares.mapIndexed { index, share ->
                    when (share.shareType) {
                        "web-link" -> {
                            val shareInfo = gson.fromJson(
                                share.shareInfo, ShareData.ShareInfo.ShareText::class.java
                            )
                            HomeWebLinkModelView(
                                id = "${share.id}",
                                iconUrl = IconUtils.getIconUrl(shareInfo.text),
                                url = shareInfo.text,
                                createdAt = share.createdAt,
                                note = share.shareNote,
                                showDivider = index < data.shareList.size - 1
                            )
                        }
                        "image" -> {
                            val shareInfo = gson.fromJson(
                                share.shareInfo, ShareData.ShareInfo.ShareImage::class.java
                            )
                            HomeImageModelView(
                                "${share.id}", shareInfo.uri, share.createdAt, share.shareNote
                            )
                        }
                        else -> {
                            null
                        }
                    }
                }.filterNotNull().forEach { it.attachTo(this) }
            }
        }
    }

    companion object {
        private const val SPAN_COUNT = 3
    }

    @Inject
    lateinit var gson: Gson

    override val viewModel: HomeViewModel by viewModels()

    private val homeAdapter = BaseListAdapter.createAdapter { layoutRes: Int, view: View ->
        return@createAdapter when (layoutRes) {
            R.layout.model_view_home_share_web_link -> HomeWebLinkModelView.HomeTextViewHolder(
                view
            )
            R.layout.model_view_home_share_image -> HomeImageModelView.HomeImageViewHolder(
                view
            )
            R.layout.model_view_loading -> LoadingViewHolder(view)
            R.layout.model_view_home_folder -> HomeFolderModelView.HomeFolderViewHolder(view) { position ->
                viewModel.onFolderClick(position)
            }
            R.layout.model_view_home_date -> HomeDateModelView.HomeDateViewHolder(view)
            else -> null
        }
    }

    override fun onCreateViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerOnBackPressHandler {
            if (viewModel.handleBackPressed()) {
                return@registerOnBackPressHandler
            }
            finish()
        }

        viewBinding.recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val model = homeAdapter.getModelAtPosition(position) ?: return 1
                    if (model is HomeFolderModelView) {
                        return 1
                    }
                    return SPAN_COUNT
                }
            }
        }

        viewBinding.recyclerView.adapter = homeAdapter
        modelViewsFactory.attach(homeAdapter)

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.reload()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDataChanged(data: HomeData) {
        modelViewsFactory.requestBuildModelViews()
        val title = data.selectedFolder?.name ?: getString(R.string.app_name)
        supportActionBar?.title = title
    }
}