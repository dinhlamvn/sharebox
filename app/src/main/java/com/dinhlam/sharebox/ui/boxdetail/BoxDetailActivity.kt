package com.dinhlam.sharebox.ui.boxdetail

import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityBoxDetailBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoxDetailActivity :
    BaseViewModelActivity<BoxDetailState, BoxDetailViewModel, ActivityBoxDetailBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener {
    override fun onCreateViewBinding(): ActivityBoxDetailBinding {
        return ActivityBoxDetailBinding.inflate(layoutInflater)
    }

    override val viewModel: BoxDetailViewModel by viewModels()

    override fun onStateChanged(state: BoxDetailState) {
        shareAdapter.requestBuildModelViews()
        viewBinding.textTitle.title = state.boxDetail?.boxName
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(this, blockShouldLoadMore = {
            getState(viewModel) { state ->
                state.canLoadMore && !state.isLoadingMore
            }
        }) {
            viewModel.loadMores()
        }
    }

    private val shareAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            LoadingModelView("top_loading").attachToIf(this) {
                state.isRefreshing
            }

            if (state.shares.isEmpty() && !state.isRefreshing) {
                add(
                    TextModelView(
                        "text_empty", getString(R.string.no_result)
                    )
                )
            } else if (state.shares.isNotEmpty()) {
                state.shares.forEach { shareDetail ->
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
                        boxDetail = shareDetail.boxDetail,
                        actionOpen = ::onOpen,
                        actionShareToOther = ::onShareToOther,
                        actionLike = ::onLike,
                        actionComment = ::onComment,
                        actionBookmark = ::onBookmark,
                        actionViewImage = ::viewImage,
                        actionViewImages = ::viewImages,
                        useGrid = false
                    ).attachTo(this)

                    SizedBoxModelView("separator_${shareDetail.shareId}").attachTo(this)
                }

                if (state.isLoadingMore) {
                    LoadingModelView("home_loading_more_${state.currentPage}").attachTo(this)
                }
            }
        }
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var userHelper: UserHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = shareAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.doOnRefresh()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun viewImages(shareId: String, uris: List<Uri>) {
        shareHelper.viewShareImages(this, shareId, uris)
    }

    private fun viewImage(shareId: String, uri: Uri) {
        shareHelper.viewShareImage(this, shareId, uri)
    }

    private fun onOpen(shareId: String) {
        startActivity(router.shareDetail(this, shareId))
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onLike(shareId: String) {
        if (!userHelper.isSignedIn()) {
            startActivity(router.signIn())
            return
        }
        viewModel.like(shareId)
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                supportFragmentManager, shareId, collectionId
            )
        }
    }

    private fun onComment(shareId: String) {
        shareHelper.showCommentDialog(supportFragmentManager, shareId)
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.bookmark(shareId, bookmarkCollectionId)
    }

    private fun viewInSource(videoSource: VideoSource, shareData: ShareData) {
        shareHelper.viewInSource(this, videoSource, shareData)
    }
}