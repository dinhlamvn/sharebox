package com.dinhlam.sharebox.ui.home.starred

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.databinding.FragmentStarredBinding
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.orElse
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.ui.comment.CommentFragment
import com.dinhlam.sharebox.ui.home.profile.ProfileState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StarredFragment :
    BaseViewModelFragment<StarredState, StarredViewModel, FragmentStarredBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentStarredBinding {
        return FragmentStarredBinding.inflate(inflater, container, false)
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = {
            return@LoadMoreLinearLayoutManager false
        }) {
            viewModel.loadMores()
        }
    }

    @Inject
    lateinit var appSharePref: AppSharePref

    private val shareAdapter = BaseListAdapter.createAdapter {
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
                    state.voteMap[shareDetail.shareId].orElse(0),
                    shareComment = shareDetail.commentCount,
                    starred = state.starredSet.contains(shareDetail.shareId),
                    actionOpen = ::onOpen,
                    actionShareToOther = ::onShareToOther,
                    actionVote = ::onVote,
                    actionComment = ::onComment,
                    actionStar = ::onStar
                )
            }
            if (models.isEmpty()) {
                add(
                    TextModelView(
                        getString(R.string.no_starred_shares), ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            } else {
                models.forEachIndexed { idx, model ->
                    add(model)
                    add(
                        SizedBoxModelView(
                            "divider_$idx",
                            height = 1.dp(),
                            backgroundColor = R.color.colorDividerLightV2
                        )
                    )
                }

                if (state.isLoadMore) {
                    add(LoadingModelView("home_load_more"))
                    add(
                        SizedBoxModelView(
                            "dividerLoadingMore",
                            height = 50.dp(),
                            backgroundColor = android.R.color.transparent
                        )
                    )
                }
            }
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    override val viewModel: StarredViewModel by viewModels()

    override fun onStateChanged(state: StarredState) {
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
            layoutManager.hadTriggerLoadMore = isLoadMore
        }
    }

    private fun onOpen(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState


        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> {
                shareHelper.openUrl(
                    requireContext(), shareData.url, appSharePref.isCustomTabEnabled()
                )
            }

            is ShareData.ShareText -> {
                shareHelper.openTextViewer(requireActivity(), shareData.text)
            }

            is ShareData.ShareImage -> {
                shareHelper.viewShareImage(requireActivity(), shareData.uri)
            }

            is ShareData.ShareImages -> {
                shareHelper.viewShareImages(requireActivity(), shareData.uris)
            }
        }
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onVote(shareId: String) {
        viewModel.vote(shareId)
    }

    private fun onStar(shareId: String) {
        viewModel.starred(shareId)
    }

    private fun onComment(shareId: String) {
        CommentFragment().apply {
            arguments = Bundle().apply {
                putString(AppExtras.EXTRA_SHARE_ID, shareId)
            }
        }.show(childFragmentManager, "CommentFragment")
    }
}