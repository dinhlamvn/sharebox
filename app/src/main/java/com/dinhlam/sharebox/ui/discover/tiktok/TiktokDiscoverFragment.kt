package com.dinhlam.sharebox.ui.discover.tiktok

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.FragmentTiktokDiscoverBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.listmodel.ChipListModel
import com.dinhlam.sharebox.listmodel.TiktokDiscoverListModel
import com.dinhlam.sharebox.recyclerview.decoration.HorizontalSpacingDecoration
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TiktokDiscoverFragment :
    BaseViewModelFragment<TiktokDiscoverState, TiktokDiscoverViewModel, FragmentTiktokDiscoverBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTiktokDiscoverBinding {
        return FragmentTiktokDiscoverBinding.inflate(layoutInflater)
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
                            requireContext(),
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
                    state.activeCategories.contains(tiktokCategory),
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
        categoryAdapter.requestBuildListModels()
        adapter.requestBuildListModels()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewCategory.addItemDecoration(HorizontalSpacingDecoration(8.dp))
        categoryAdapter.attachTo(binding.recyclerViewCategory, this)
        adapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refresh()
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