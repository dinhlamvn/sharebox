package com.dinhlam.sharebox.ui.home.community

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.FragmentCommunityBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.dialog.guideline.GuidelineDialogFragment
import com.dinhlam.sharebox.dialog.singlechoice.SingleChoiceBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.modelview.BoxModelView
import com.dinhlam.sharebox.modelview.CarouselModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.modelview.grid.GridUrlModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreGridLayoutManager
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.LiveEventUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommunityFragment :
    BaseViewModelFragment<CommunityState, CommunityViewModel, FragmentCommunityBinding>(),
    BoxSelectionDialogFragment.OnBoxSelectedListener,
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    SingleChoiceBottomSheetDialogFragment.OnOptionItemSelectedListener {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentCommunityBinding {
        return FragmentCommunityBinding.inflate(inflater, container, false)
    }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val boxId = result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)
                    ?: return@registerForActivityResult
                viewModel.setBox(boxId)
            }
        }

    private val layoutManager by lazy {
        LoadMoreGridLayoutManager(requireContext(), 2, blockShouldLoadMore = {
            return@LoadMoreGridLayoutManager getState(viewModel) { state -> state.canLoadMore && !state.isLoadingMore }
        }) {
            viewModel.loadMores()
        }.apply {
            spanSizeLookup = BaseSpanSizeLookup(shareAdapter, 2)
        }
    }

    private val shareAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView("top_loading"))
            }

            if (state.boxes.isNotEmpty()) {
                val lastIndex = state.boxes.size - 1
                val boxModelViews = state.boxes.mapIndexed { idx, boxDetail ->
                    BoxModelView(
                        "box_${boxDetail.boxId}", boxDetail.boxName, boxDetail.boxDesc, Spacing.All(
                            if (idx == 0) 16.dp() else 8.dp(),
                            16.dp(),
                            if (idx == lastIndex) 16.dp() else 8.dp(),
                            16.dp()
                        ), BaseListAdapter.NoHashProp(View.OnClickListener {
                            viewModel.setBox(boxDetail)
                        })
                    )
                }
                add(CarouselModelView("carousel_box", boxModelViews))

                add(SizedBoxModelView("divider_carousel", height = 1.dp()))
            }

            if (state.shares.isEmpty() && !state.isRefreshing) {
                add(
                    TextModelView(
                        "text_empty", getString(R.string.no_result)
                    )
                )
            } else if (state.shares.isNotEmpty()) {
                state.shares.forEach { shareDetail ->
                    val modelView = shareDetail.shareData.buildShareModelViews(
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
                        actionBoxClick = ::onBoxClick,
                        useGrid = true,
                        actionMore = ::onMore
                    )

                    add(modelView)

                    if (modelView !is GridUrlModelView) {
                        add(SizedBoxModelView("separator_${shareDetail.shareId}"))
                    }
                }

                if (state.canLoadMore) {
                    add(LoadingModelView("home_load_more_${state.currentPage}"))
                } else if (state.isLoadingMore) {
                    add(LoadingModelView("home_loading_more_${state.currentPage}"))
                }
            }
        }
    }

    private fun onMore(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

        val items = arrayListOf(
            SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(0, getString(R.string.shares)),
            SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(0, getString(R.string.bookmark))
        )

        if (share.isVideoShare) {
            items.add(SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(0, getString(R.string.download)))
            items.add(SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(0, getString(R.string.view_in_source)))
        }

        SingleChoiceBottomSheetDialogFragment().apply {
            arguments = bundleOf(
                AppExtras.EXTRA_CHOICE_ITEMS to items.toTypedArray(),
                AppExtras.EXTRA_SHARE_ID to shareId
            )
        }.show(childFragmentManager, "SingleChoiceBottomSheetDialogFragment")
    }

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        getState(viewModel) { state ->
            val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return@getState
            val share =
                state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

            when (position) {
                0 -> shareHelper.shareToOther(share)
                1 -> onBookmark(shareId)
                2 -> viewModel.saveVideoToGallery(shareId, requireContext())
                3 -> viewModel.viewInSource(shareId) {
                    viewInSource(it.videoSource, share.shareData)
                }
            }
        }
    }

    private fun onBoxClick(boxDetail: BoxDetail?) {
        viewModel.setBox(boxDetail)
    }

    private fun viewImages(shareId: String, uris: List<Uri>) {
        shareHelper.viewShareImages(requireActivity(), shareId, uris)
    }

    private fun viewImage(shareId: String, uri: Uri) {
        shareHelper.viewShareImage(requireActivity(), shareId, uri)
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var userHelper: UserHelper

    override val viewModel: CommunityViewModel by viewModels()

    override fun onStateChanged(state: CommunityState) {
        shareAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LiveEventUtils.eventScrollToTopCommunity.observe(viewLifecycleOwner) { shouldScroll ->
            if (shouldScroll) {
                viewBinding.recyclerView.smoothScrollToPosition(0)
                viewModel.doOnRefresh()
            }
        }

        viewBinding.toolbar.inflateMenu(R.menu.community_menu)
        viewBinding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_create_box -> createNewBox()
                R.id.menu_guideline -> showGuideline()
            }
            true
        }

        viewBinding.textTitle.setOnClickListener {
            shareHelper.showBoxSelectionDialog(childFragmentManager)
        }

        viewModel.consume(this, CommunityState::currentBox) { currentBox ->
            viewBinding.textTitle.text = currentBox?.boxName ?: getString(R.string.box_community)
        }

        shareAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    layoutManager.scrollToPositionWithOffset(0, 0)
                }
            }
        })

        viewBinding.recyclerView.itemAnimator?.cast<DefaultItemAnimator>()?.supportsChangeAnimations =
            false
        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = shareAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.doOnRefresh()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.consume(this, CommunityState::isLoadingMore) { isLoadMore ->
            layoutManager.hadTriggerLoadMore = isLoadMore
        }

        if (appSharePref.isShowGuideline()) {
            appSharePref.offShowGuideline()
            showGuideline()
        }
    }

    private fun createNewBox() {
        if (!userHelper.isSignedIn()) {
            showToast(R.string.require_sign_in_to_create_box)
            startActivity(router.signIn())
            return
        }

        createBoxResultLauncher.launch(router.boxIntent(requireContext()))
    }

    private fun showGuideline() {
        GuidelineDialogFragment().show(childFragmentManager, "GuidelineDialogFragment")
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
        if (!userHelper.isSignedIn()) {
            startActivity(router.signIn())
            return
        }
        viewModel.like(shareId)
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                childFragmentManager, shareId, collectionId
            )
        }
    }

    private fun onComment(shareId: String) {
        shareHelper.showCommentDialog(childFragmentManager, shareId)
    }

    override fun onBoxSelected(boxId: String) {
        viewModel.setBox(boxId)
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.bookmark(shareId, bookmarkCollectionId)
    }

    private fun viewInSource(videoSource: VideoSource, shareData: ShareData) {
        shareHelper.viewInSource(requireContext(), videoSource, shareData)
    }
}