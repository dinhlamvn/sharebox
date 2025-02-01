package com.dinhlam.sharebox.ui.discover.zingnews

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityZingnewsDiscoverBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.listmodel.ChipListModel
import com.dinhlam.sharebox.listmodel.ZingNewsDiscoverListModel
import com.dinhlam.sharebox.model.ZingNewsDiscover
import com.dinhlam.sharebox.recyclerview.decoration.HorizontalSpacingDecoration
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ZingNewsDiscoverActivity :
    BaseViewModelActivity<ZingNewsDiscoverState, ZingNewDiscoverViewModel, ActivityZingnewsDiscoverBinding>() {

    override fun onCreateViewBinding(): ActivityZingnewsDiscoverBinding {
        return ActivityZingnewsDiscoverBinding.inflate(layoutInflater)
    }

    private val chooseBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                viewModel.setCurrentBoxId(boxId)
            }
        }

    @Inject
    lateinit var router: Router

    private val adapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            state.zingNewsDiscovers.forEach { zingNewsDiscover ->
                ZingNewsDiscoverListModel(
                    "news_${zingNewsDiscover.url}",
                    zingNewsDiscover.url,
                    zingNewsDiscover.title,
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        onClick(zingNewsDiscover)
                    }),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        onArchive(zingNewsDiscover.url)
                    })
                ).attachTo(this)
            }
        }
    }

    private val categoryAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            state.zingNewsCategories.forEach { zingNewsCategory ->
                ChipListModel(
                    "category_${zingNewsCategory.id}",
                    zingNewsCategory.name,
                    zingNewsCategory.id == state.zingNewsCategory?.id,
                    BaseListAdapter.NoHashProp(
                        View.OnClickListener {
                            viewModel.setActiveCategory(zingNewsCategory)
                        })
                ).attachTo(this)
            }
        }
    }

    override val viewModel: ZingNewDiscoverViewModel by viewModels()

    override fun onStateChanged(state: ZingNewsDiscoverState) {
        binding.loading.toggle(state.asyncLoadZingNewsDiscover is BaseViewModel.AsyncLoad.Loading)
        val boxName = state.currentBox?.boxName
        val isLock = state.currentBox?.passcode?.isNotBlank() ?: false
        binding.textShareBox.text = boxName
        binding.textShareBox.setDrawableCompat(
            start = Icons.boxIcon(this) { copy(sizeDp = 20) },
            end = if (isLock) Icons.lockIcon(this) { copy(sizeDp = 16) } else null,
        )
        categoryAdapter.requestBuildListModels()
        adapter.requestBuildListModels()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        binding.recyclerViewCategory.addItemDecoration(HorizontalSpacingDecoration(8.dp))
        categoryAdapter.attachTo(binding.recyclerViewCategory, this)
        adapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            //viewModel.refresh()
        }

        binding.containerShareBox.setOnClickListener {
            chooseBoxLauncher.launch(router.boxList(this, null))
        }

        onChange(ZingNewsDiscoverState::asyncLoadArchive) { asyncLoad ->
            if (asyncLoad.success) {
                showToast(getString(R.string.archive_url_success, asyncLoad.data))
            }
        }
    }

    private fun onClick(zingNewsDiscover: ZingNewsDiscover) = getState(viewModel) { state ->
        router.moveToChromeCustomTab(
            this@ZingNewsDiscoverActivity,
            zingNewsDiscover.url,
            state.currentBox?.boxId,
            state.currentBox?.boxName,
            false
        )
    }

    private fun onArchive(url: String) = getState(viewModel) { state ->
        val box =
            getState(viewModel, ZingNewsDiscoverState::currentBox) ?: return@getState showToast(
                R.string.please_choose_box
            )
        viewModel.archiveLink(url, box.boxId)
    }
}