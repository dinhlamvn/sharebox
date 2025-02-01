package com.dinhlam.sharebox.ui.discover.tiktok

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityTiktokDiscoverBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.listmodel.ChipListModel
import com.dinhlam.sharebox.listmodel.TiktokDiscoverListModel
import com.dinhlam.sharebox.recyclerview.decoration.HorizontalSpacingDecoration
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TiktokDiscoverActivity :
    BaseViewModelActivity<TiktokDiscoverState, TiktokDiscoverViewModel, ActivityTiktokDiscoverBinding>() {

    override fun onCreateViewBinding(): ActivityTiktokDiscoverBinding {
        return ActivityTiktokDiscoverBinding.inflate(layoutInflater)
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
            state.tiktokDiscoverList.forEach { tiktokDiscover ->
                TiktokDiscoverListModel(
                    tiktokDiscover.id,
                    tiktokDiscover.url,
                    tiktokDiscover.desc,
                    tiktokDiscover.playCount,
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        startActivity(router.viewIntent(tiktokDiscover.url))
                    }),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        onArchive(tiktokDiscover.url)
                    }),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        WorkerUtils.enqueueJobDownloadTiktokVideo(
                            this@TiktokDiscoverActivity,
                            tiktokDiscover.hashCode(),
                            tiktokDiscover.url
                        )
                    }),
                ).attachTo(this)
            }
        }
    }

    private val categoryAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            state.categories.forEach { tiktokCategory ->
                ChipListModel(
                    "category_${tiktokCategory.categoryId}",
                    tiktokCategory.categoryName,
                    tiktokCategory.categoryId == state.activeCategory?.categoryId,
                    BaseListAdapter.NoHashProp(
                        View.OnClickListener {
                            viewModel.setActiveCategory(tiktokCategory)
                        })
                ).attachTo(this)
            }
        }
    }

    override val viewModel: TiktokDiscoverViewModel by viewModels()

    override fun onStateChanged(state: TiktokDiscoverState) {
        binding.loading.toggle(state.asyncLoadTiktokDiscover is BaseViewModel.AsyncLoad.Loading)
        val boxName = state.currentBox?.boxName
        val isLock = state.currentBox?.passcode?.isNotBlank() ?: false
        binding.textShareBox.text = boxName
        binding.textShareBox.setDrawableCompat(
            start = Icons.boxIcon(this) { copy(sizeDp = 20) },
            end = if (isLock) Icons.lockIcon(this) { copy(sizeDp = 16) } else null,
        )
        categoryAdapter.requestBuildListModels {
            val position =
                getState(viewModel) { state -> state.categories.indexOfFirst { cate -> cate.categoryId == state.activeCategory?.categoryId } }
            if (position >= 0) {
                binding.recyclerViewCategory.layoutManager?.cast<LinearLayoutManager>()
                    ?.scrollToPositionWithOffset(position, 20.dp)
            }
        }
        adapter.requestBuildListModels()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressHandler {
            if (binding.recyclerView.computeVerticalScrollOffset() == 0) {
                finish()
            } else {
                binding.recyclerView.smoothScrollToPosition(0)
            }
        }

        setSupportActionBar(binding.toolbar)

        binding.recyclerViewCategory.addItemDecoration(HorizontalSpacingDecoration(8.dp))
        categoryAdapter.attachTo(binding.recyclerViewCategory, this)
        adapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refresh()
        }

        binding.containerShareBox.setOnClickListener {
            chooseBoxLauncher.launch(router.boxList(this, null))
        }

        onChange(TiktokDiscoverState::asyncLoadArchive) { asyncLoad ->
            if (asyncLoad.success) {
                showToast(getString(R.string.archive_url_success, asyncLoad.data))
            }
        }
    }

    private fun onArchive(url: String) = getState(viewModel) {
        val box = getState(viewModel, TiktokDiscoverState::currentBox) ?: return@getState showToast(
            R.string.please_choose_box
        )
        viewModel.archiveLink(url, box.boxId)
    }
}