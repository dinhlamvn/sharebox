package com.dinhlam.sharebox.ui.discover.tiktok

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
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
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TiktokDiscoverListModel
import com.dinhlam.sharebox.model.TiktokDiscover
import com.dinhlam.sharebox.recyclerview.LoadMoreGridLayoutManager
import com.dinhlam.sharebox.recyclerview.decoration.HorizontalSpacingDecoration
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.discover.tiktok.viewer.TiktokDiscoverVideoViewerDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TiktokDiscoverFragment :
    BaseViewModelFragment<TiktokDiscoverState, TiktokDiscoverViewModel, FragmentTiktokDiscoverBinding>(),
    TiktokDiscoverVideoViewerDialogFragment.OnDialogCallback {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTiktokDiscoverBinding {
        return FragmentTiktokDiscoverBinding.inflate(layoutInflater)
    }

    override val isOverrideBackPressedCallback: Boolean
        get() = true

    private val gridLayoutManager by lazy {
        LoadMoreGridLayoutManager(
            requireContext(),
            2,
            { getState(viewModel, TiktokDiscoverState::isLoadingMore) },
            viewModel::loadMore
        )
    }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { boxId ->
                    viewModel.setCurrentBoxId(boxId)
                }
            }
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
                    tiktokDiscover.diggCount,
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        onViewTiktokVideo(tiktokDiscover)
                    }),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        onArchive(tiktokDiscover.url, tiktokDiscover.desc)
                    }),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        startActivity(
                            router.downloadBottomSheet(
                                requireContext(),
                                listOf(tiktokDiscover.url)
                            )
                        )
                    }),
                ).attachTo(this)
            }

            if (state.isLoadingMore) {
                LoadingListModel("loading_more").attachTo(this)
            }
        }
    }

    private val categoryAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            state.categories.forEach { tiktokCategory ->
                ChipListModel(
                    "category_${tiktokCategory.categoryId}",
                    tiktokCategory.categoryName,
                    state.activeCategory.categoryId == tiktokCategory.categoryId,
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
        categoryAdapter.requestBuildListModels {
            val activePosition = state.categories.indexOf(state.activeCategory)
            if (activePosition >= 0) {
                binding.recyclerViewCategory.scrollToPosition(activePosition)
            }
        }
        adapter.requestBuildListModels()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewCategory.addItemDecoration(HorizontalSpacingDecoration(8.dp))
        categoryAdapter.attachTo(binding.recyclerViewCategory, this)

        binding.recyclerView.layoutManager = gridLayoutManager
        adapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refresh()
        }

        binding.boxSectionButton.setOnClickListener {
            chooseBoxLauncher.launch(router.boxList(requireContext(), null))
        }

        binding.boxSectionButton.setOnAddIconClickListener {
            createBoxResultLauncher.launch(router.boxForm(requireContext(), null))
        }

        onChange(TiktokDiscoverState::asyncLoadArchive) { asyncLoad ->
            if (asyncLoad.success) {
                showToast(getString(R.string.archive_url_success, asyncLoad.data))
            }
        }

        onChange(TiktokDiscoverState::currentBox) { currentBox ->
            val boxName = currentBox?.boxName
            val isLock = currentBox?.passcode?.isNotBlank() ?: false
            binding.boxSectionButton.setBoxName(boxName)
            binding.boxSectionButton.showLock(isLock)
        }
    }

    private fun onArchive(url: String, note: String?) = getState(viewModel) {
        val box = getState(viewModel, TiktokDiscoverState::currentBox)
        if (box == null) {
            showToast(R.string.please_choose_box)
            binding.boxSectionButton.playZoomAnimation()
            return@getState
        }
        viewModel.archiveLink(url, note, box.boxId)
    }

    private fun onViewTiktokVideo(tiktokDiscover: TiktokDiscover) {
        binding.loading.toggle(true)
        viewModel.loadTiktokVideo(tiktokDiscover) { videoUrl ->
            binding.loading.toggle(false)
            if (videoUrl == null) {
                return@loadTiktokVideo showToast(R.string.video_not_available)
            }
            TiktokDiscoverVideoViewerDialogFragment()
                .apply {
                    arguments = bundleOf(
                        AppExtras.EXTRA_URL to videoUrl,
                        TiktokDiscoverVideoViewerDialogFragment.EXTRA_VIEW_DESC to tiktokDiscover.desc,
                        TiktokDiscoverVideoViewerDialogFragment.EXTRA_VIEW_TIKTOK_URL to tiktokDiscover.url,
                        TiktokDiscoverVideoViewerDialogFragment.EXTRA_VIEW_COUNT to tiktokDiscover.playCount.toInt(),
                        TiktokDiscoverVideoViewerDialogFragment.EXTRA_LIKE_COUNT to tiktokDiscover.diggCount.toInt()
                    )
                }.show(childFragmentManager, "tiktok_discover_video_viewer")
        }
    }

    override fun onSave(url: String, note: String?) {
        onArchive(url, note)
    }

    override fun onBackPressed() {
        if (binding.recyclerView.computeVerticalScrollOffset() == 0) {
            activity?.finish()
        } else {
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }
}