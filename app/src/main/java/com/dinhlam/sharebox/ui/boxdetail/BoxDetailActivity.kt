package com.dinhlam.sharebox.ui.boxdetail

import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityBoxDetailBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.optionmenu.OptionMenuBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.buildShareListModel
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoxDetailActivity :
    BaseViewModelActivity<BoxDetailState, BoxDetailViewModel, ActivityBoxDetailBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    OptionMenuBottomSheetDialogFragment.OnOptionItemSelectedListener {
    override fun onCreateViewBinding(): ActivityBoxDetailBinding {
        return ActivityBoxDetailBinding.inflate(layoutInflater)
    }

    override val viewModel: BoxDetailViewModel by viewModels()

    override fun onStateChanged(state: BoxDetailState) {
        shareAdapter.requestBuildModelViews()
        binding.textTitle.text = state.boxDetail?.boxName
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
            LoadingListModel("top_loading").attachTo(this) {
                state.isRefreshing
            }

            if (state.shares.isEmpty() && !state.isRefreshing) {
                TextListModel(
                    "text_empty", getString(R.string.no_result)
                ).attachTo(this)
            } else if (state.shares.isNotEmpty()) {
                state.shares.forEach { shareDetail ->
                    shareDetail.shareData.buildShareListModel(
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
                        actionViewImage = ::viewImage,
                        actionViewImages = ::viewImages
                    ).attachTo(this)
                }

                LoadingListModel("home_loading_more_${state.currentPage}").attachTo(this) { state.canLoadMore && state.shares.size > 3 }
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

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = shareAdapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.doOnRefresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.textTitle.setDrawableCompat(Icons.boxIcon(this))
    }

    private fun viewImages(shareId: String, uris: List<Uri>) {
        shareHelper.viewShareImages(this, shareId, uris)
    }

    private fun viewImage(shareId: String, uri: Uri) {
        shareHelper.viewShareImage(this, shareId, uri)
    }

    private fun onOpen(shareId: String) = getState(viewModel) { state ->
        val share = state.shares.firstOrNull { shareDetail -> shareDetail.shareId == shareId }
            ?: return@getState
        openShare(share)
    }

    private fun openShare(share: ShareDetail) {
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> router.moveToBrowser(shareData.url)
            is ShareData.ShareText -> {
                shareHelper.openTextViewerDialog(this, shareData.text)
            }

            is ShareData.ShareImage -> shareHelper.viewShareImage(
                this, share.shareId, shareData.uri
            )

            is ShareData.ShareImages -> shareHelper.viewShareImages(
                this, share.shareId, shareData.uris
            )
        }
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.showMore(this, share)
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

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        getState(viewModel) { state ->
            val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return@getState
            val share =
                state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

            when (position) {
                0 -> shareHelper.shareToOther(share)
                1 -> WorkerUtils.enqueueDownloadShare(
                    this, share.shareData.cast<ShareData.ShareUrl>()?.url, share
                )

                2 -> onBookmark(shareId)
                3 -> copy(share.boxDetail?.boxId)
            }
        }
    }
}