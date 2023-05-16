package com.dinhlam.sharebox.ui.home.profile

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
import com.dinhlam.sharebox.databinding.FragmentProfileBinding
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.profile.ProfileInfoModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.ui.comment.CommentFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment :
    BaseViewModelFragment<ProfileState, ProfileViewModel, FragmentProfileBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = { false }) {
            viewModel.loadMores()
        }
    }

    private val adapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            val nonNullUser = state.activeUser ?: return@getState run {
                add(LoadingModelView("loading_user"))
            }

            add(
                ProfileInfoModelView(
                    nonNullUser.id,
                    nonNullUser.avatar,
                    nonNullUser.name,
                    nonNullUser.drama,
                    nonNullUser.level,
                    nonNullUser.createdAt
                )
            )
            add(
                SizedBoxModelView(
                    "divider",
                    backgroundColor = android.R.color.transparent,
                    height = 16.dp()
                )
            )
            add(
                SizedBoxModelView(
                    "divider1",
                    backgroundColor = R.color.colorDividerLightV2,
                    height = 1.dp()
                )
            )

            if (state.isRefreshing) {
                add(LoadingModelView("loading_profile"))
                return@getState
            }

            val models = state.shares.map { shareDetail ->
                shareDetail.shareData.buildShareModelViews(
                    requireContext(),
                    shareDetail.shareId,
                    shareDetail.createdAt,
                    shareDetail.shareNote,
                    shareDetail.user,
                    0,
                    shareComment = shareDetail.commentCount,
                    starred = false,
                    actionOpen = ::onOpen,
                    actionShareToOther = ::onShareToOther,
                    actionVote = ::onVote,
                    actionComment = ::onComment,
                    actionStar = ::onStar
                )
            }
            if (models.isNotEmpty()) {
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
                    add(LoadingModelView("loading_more"))
                }
            }
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    override val viewModel: ProfileViewModel by viewModels()

    override fun onStateChanged(state: ProfileState) {
        adapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = adapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewBinding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnRefresh()
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
        //viewModel.vote(shareId)
    }

    private fun onStar(shareId: String) {
        //viewModel.starred(shareId)
    }

    private fun onComment(shareId: String) {
        CommentFragment().apply {
            arguments = Bundle().apply {
                putString(AppExtras.EXTRA_SHARE_ID, shareId)
            }
        }.show(childFragmentManager, "CommentFragment")
    }
}