package com.dinhlam.sharebox.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.databinding.FragmentProfileBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.profile.ProfileInfoModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.AppRouter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment :
    BaseViewModelFragment<ProfileState, ProfileViewModel, FragmentProfileBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = {
            getState(viewModel) { state ->
                state.canLoadMore && !state.isLoadingMore
            }
        }) {
            viewModel.loadMores()
        }
    }

    private val adapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            val nonNullUser = state.currentUser ?: return@getState run {
                add(LoadingModelView("loading_user"))
            }

            add(
                ProfileInfoModelView(
                    nonNullUser.id,
                    nonNullUser.avatar,
                    nonNullUser.name,
                    nonNullUser.drama,
                    nonNullUser.level,
                    nonNullUser.joinDate,
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        openSettingPage()
                    })
                )
            )
            add(
                SizedBoxModelView("divider", height = 8.dp())
            )

            if (state.isRefreshing) {
                add(LoadingModelView("loading_shares"))
            }
            if (state.shares.isNotEmpty()) {
                state.shares.map { shareDetail ->
                    shareDetail.shareData.buildShareModelViews(
                        screenHeight(),
                        shareDetail.shareId,
                        shareDetail.shareDate,
                        shareDetail.shareNote,
                        shareDetail.user,
                        shareDetail.likeNumber,
                        commentNumber = shareDetail.commentNumber,
                        bookmarked = shareDetail.bookmarked,
                        liked = shareDetail.liked,
                        actionOpen = ::onOpen,
                        actionShareToOther = ::onShareToOther,
                        actionLike = ::onLike,
                        actionComment = ::onComment,
                        actionBookmark = ::onBookmark
                    )
                }.forEach { model ->
                    add(model)
                    add(
                        SizedBoxModelView(
                            "divider_${model.modelId}",
                            height = 8.dp(),
                        )
                    )
                }

                if (state.isLoadingMore) {
                    add(LoadingModelView("loading_more_${state.currentPage}"))
                }
            }
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var appRouter: AppRouter

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

        viewModel.consume(this, ProfileState::isLoadingMore, true) { isLoadMore ->
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
                shareHelper.openTextViewerDialog(requireActivity(), shareData.text)
            }

            else -> {}
        }
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onLike(shareId: String) {
        viewModel.like(shareId)
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                childFragmentManager,
                shareId,
                collectionId
            )
        }
    }

    private fun onComment(shareId: String) {
        shareHelper.showCommentDialog(childFragmentManager, shareId)
    }

    private fun openSettingPage() {
        startActivity(appRouter.settingIntent())
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.bookmark(shareId, bookmarkCollectionId)
    }
}