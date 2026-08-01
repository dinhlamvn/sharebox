package com.dinhlam.sharebox.ui.discover.pinterest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.databinding.FragmentPinterestDiscoverBinding
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.PinterestPinListModel
import com.dinhlam.sharebox.recyclerview.LoadMoreGridLayoutManager
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PinterestDiscoverFragment :
    BaseViewModelFragment<PinterestDiscoverState, PinterestDiscoverViewModel, FragmentPinterestDiscoverBinding>() {

    @Inject
    lateinit var router: Router

    override val viewModel: PinterestDiscoverViewModel by viewModels()

    private val gridLayoutManager by lazy {
        LoadMoreGridLayoutManager(
            requireContext(),
            2,
            { getState(viewModel, PinterestDiscoverState::isLoadingMore) },
            viewModel::loadMore,
        )
    }

    private val adapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            state.pins.forEach { pin ->
                PinterestPinListModel(
                    id = "pinterest_${pin.id}",
                    imageUrl = pin.imageUrl,
                    title = pin.title,
                    onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        router.moveToChromeCustomTab(requireContext(), pin.url, null, null)
                    }),
                ).attachTo(this)
            }

            if (state.isLoadingMore) {
                LoadingListModel("pinterest_loading_more").attachTo(this)
            }
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentPinterestDiscoverBinding {
        return FragmentPinterestDiscoverBinding.inflate(inflater, container, false)
    }

    override fun onStateChanged(state: PinterestDiscoverState) {
        val loading = state.asyncSearch is BaseViewModel.AsyncLoad.Loading
        binding.loading.toggle(loading)
        binding.searchButton.isEnabled = !loading
        binding.openPinterest.isVisible = state.searchUrl != null
        binding.emptyText.isVisible =
            state.asyncSearch.completed && !loading && state.pins.isEmpty()
        adapter.requestBuildListModels()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = gridLayoutManager
        adapter.attachTo(binding.recyclerView, viewLifecycleOwner)

        binding.searchButton.setOnClickListener {
            submitSearch()
        }
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitSearch()
                true
            } else {
                false
            }
        }
        binding.openPinterest.setOnClickListener {
            getState(viewModel, PinterestDiscoverState::searchUrl)?.let { url ->
                router.moveToChromeCustomTab(requireContext(), url, null, null)
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refresh()
        }
    }

    private fun submitSearch() {
        val query = binding.searchInput.text.trimmedString()
        binding.searchInputLayout.error = if (query.isBlank()) {
            getString(com.dinhlam.sharebox.R.string.search_text_required)
        } else {
            null
        }
        if (query.isNotBlank()) {
            viewModel.search(query)
        }
    }
}
