package com.dinhlam.sharebox.ui.home.community

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.databinding.FragmentCommunityBinding
import com.dinhlam.sharebox.databinding.ModelViewDividerBinding
import com.dinhlam.sharebox.databinding.ModelViewLoadingBinding
import com.dinhlam.sharebox.databinding.ModelViewShareListImageBinding
import com.dinhlam.sharebox.databinding.ModelViewShareListImagesBinding
import com.dinhlam.sharebox.databinding.ModelViewShareListTextBinding
import com.dinhlam.sharebox.databinding.ModelViewShareListUrlBinding
import com.dinhlam.sharebox.databinding.ModelViewSingleTextBinding
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.modelview.DividerModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SingleTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListImageModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListImagesModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListUrlModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.ui.home.profile.ProfileState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommunityFragment :
    BaseViewModelFragment<CommunityState, CommunityViewModel, FragmentCommunityBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentCommunityBinding {
        return FragmentCommunityBinding.inflate(inflater, container, false)
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = {
            return@LoadMoreLinearLayoutManager false
        }) {
            viewModel.loadMores()
        }
    }

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var appSharePref: AppSharePref

    private val shareAdapter = BaseListAdapter.createAdapter({
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView("home_refresh"))
                return@getState
            }

            val models = state.shares.map { shareDetail ->
                shareDetail.shareData.buildShareModelViews(
                    requireContext(),
                    shareDetail.shareId,
                    shareDetail.createdAt,
                    shareDetail.shareNote,
                    shareDetail.user,
                    state.voteMap[shareDetail.shareId].orElse(0)
                )
            }
            if (models.isEmpty()) {
                add(
                    SingleTextModelView(
                        getString(R.string.recently_empty), ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            } else {
                models.forEachIndexed { idx, model ->
                    add(model)
                    add(DividerModelView("divider_$idx", size = 8))
                }

                if (state.isLoadMore) {
                    add(LoadingModelView("home_load_more"))
                    add(
                        DividerModelView(
                            "dividerLoadingMore", size = 50, color = android.R.color.transparent
                        )
                    )
                }
            }
        }
    }) {
        withViewType(R.layout.model_view_loading) {
            LoadingModelView.LoadingViewHolder(ModelViewLoadingBinding.bind(this))
        }

        withViewType(R.layout.model_view_divider) {
            DividerModelView.DividerViewHolder(ModelViewDividerBinding.bind(this))
        }

        withViewType(R.layout.model_view_single_text) {
            SingleTextModelView.SingleTextViewHolder(ModelViewSingleTextBinding.bind(this))
        }

        withViewType(R.layout.model_view_share_list_text) {
            ShareListTextModelView.ShareListTextViewHolder(
                ModelViewShareListTextBinding.bind(this), { textContent ->
                    val dialog = TextViewerDialogFragment()
                    dialog.arguments = Bundle().apply {
                        putString(Intent.EXTRA_TEXT, textContent)
                    }
                    dialog.show(parentFragmentManager, "TextViewerDialogFragment")
                }, ::shareToOther, ::voteShare
            )
        }

        withViewType(R.layout.model_view_share_list_url) {
            ShareListUrlModelView.ShareListUrlWebHolder(
                ModelViewShareListUrlBinding.bind(this), ::openWebLink, ::shareToOther, ::voteShare
            )
        }

        withViewType(R.layout.model_view_share_list_image) {
            ShareListImageModelView.ShareListImageViewHolder(
                ModelViewShareListImageBinding.bind(this), ::shareToOther, { uri ->
                    shareHelper.viewShareImage(requireActivity(), uri)
                }, ::voteShare
            )
        }

        withViewType(R.layout.model_view_share_list_images) {
            ShareListImagesModelView.ShareListImagesViewHolder(
                ModelViewShareListImagesBinding.bind(this), ::shareToOther, { uris ->
                    shareHelper.viewShareImages(requireActivity(), uris)
                }, ::voteShare
            )
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    override val viewModel: CommunityViewModel by viewModels()

    override fun onStateChanged(state: CommunityState) {
        shareAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.itemAnimator = null
        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = shareAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.doOnRefresh()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.consume(this, ProfileState::isLoadMore, true) { isLoadMore ->
            layoutManager.isLoadMore = isLoadMore
        }
    }

    private fun openWebLink(url: String) {
        if (appSharePref.isCustomTabEnabled()) {
            appRouter.moveToChromeCustomTab(requireContext(), url)
        } else {
            appRouter.moveToBrowser(url)
        }
    }

    private fun shareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun voteShare(shareId: String) {
        viewModel.vote(shareId)
    }
}