package com.dinhlam.sharebox.ui.home.community

import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.model.BoxDetail
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.databinding.FragmentCommunityBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.dialog.guideline.GuidelineDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.queryIntentActivitiesCompat
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.modelview.BoxModelView
import com.dinhlam.sharebox.modelview.CarouselModelView
import com.dinhlam.sharebox.modelview.CommentModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.modelview.videomixer.VideoModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.utils.LiveEventUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommunityFragment :
    BaseViewModelFragment<CommunityState, CommunityViewModel, FragmentCommunityBinding>(),
    BoxSelectionDialogFragment.OnBoxSelectedListener,
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener {

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
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = {
            return@LoadMoreLinearLayoutManager getState(viewModel) { state -> state.canLoadMore && !state.isLoadingMore }
        }) {
            viewModel.loadMores()
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
                    val videoMixer = state.videoMixers[shareDetail.shareId]
                    val modelView = videoMixer?.let { videoMixerDetail ->
                        VideoModelView(
                            "video_${videoMixerDetail.originUrl}_${videoMixerDetail.id}",
                            videoMixerDetail.originUrl,
                            videoMixerDetail.shareDetail,
                            actionViewInSource = BaseListAdapter.NoHashProp(::viewInSource),
                            actionShareToOther = BaseListAdapter.NoHashProp(::onShareToOther),
                            actionLike = BaseListAdapter.NoHashProp(::onLike),
                            actionComment = BaseListAdapter.NoHashProp(::onComment),
                            actionBookmark = BaseListAdapter.NoHashProp(::onBookmark),
                            actionSaveToGallery = BaseListAdapter.NoHashProp(::onSaveToGallery)
                        )
                    } ?: shareDetail.shareData.buildShareModelViews(
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
                        actionBoxClick = ::onBoxClick
                    )
                    add(modelView)

                    shareDetail.commentDetail?.let { commentDetail ->
                        CommentModelView(
                            commentDetail.id,
                            commentDetail.userDetail.name,
                            commentDetail.userDetail.avatar,
                            commentDetail.content,
                            commentDetail.commentDate
                        )
                    }?.let { commentModelView ->
                        add(
                            SizedBoxModelView(
                                "divider_comment_${commentModelView.modelId}",
                                height = 1.dp(),
                            )
                        )
                        add(commentModelView)
                    }

                    add(
                        SizedBoxModelView(
                            "divider_${shareDetail.shareId}",
                            height = 8.dp(),
                        )
                    )
                }

                if (state.canLoadMore) {
                    add(LoadingModelView("home_load_more_${state.currentPage}"))
                } else if (state.isLoadingMore) {
                    add(LoadingModelView("home_loading_more_${state.currentPage}"))
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
    lateinit var appRouter: AppRouter

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
            startActivity(appRouter.signIn())
            return
        }

        createBoxResultLauncher.launch(appRouter.boxIntent(requireContext()))
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
            startActivity(appRouter.signIn())
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

    private fun viewInFacebook(shareData: ShareData) {
        val shareUrl = shareData.cast<ShareData.ShareUrl>() ?: return
        val viewIntent = appRouter.viewIntent(shareUrl.url)
        viewIntent.setPackage(AppConsts.FACEBOOK_M_PACKAGE_ID)

        if (viewIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(viewIntent)
        } else {
            startActivity(appRouter.playStoreIntent(AppConsts.FACEBOOK_M_PACKAGE_ID))
        }
    }

    private fun viewInYoutube(shareData: ShareData) {
        val shareUrl = shareData.cast<ShareData.ShareUrl>() ?: return
        val viewIntent = appRouter.viewIntent(shareUrl.url)
        viewIntent.runCatching {
            startActivity(this)
        }.onFailure { error ->
            Logger.error(error)
            startActivity(appRouter.playStoreIntent(AppConsts.YOUTUBE_M_PACKAGE_ID))
        }
    }

    private fun viewInSource(shareData: ShareData) {
        val shareUrl = shareData.cast<ShareData.ShareUrl>() ?: return
        val viewIntent = appRouter.viewIntent(shareUrl.url)

        val resolveInfoList = context?.packageManager?.queryIntentActivitiesCompat(
            viewIntent, PackageManager.GET_META_DATA
        ) ?: return

        resolveInfoList.run stop@{
            forEach { resolveInfo ->
                if (resolveInfo.activityInfo.packageName.equals(AppConsts.TIKTOK_M_PACKAGE_ID)) {
                    viewIntent.setPackage(AppConsts.TIKTOK_M_PACKAGE_ID)
                    return@stop
                }

                if (resolveInfo.activityInfo.packageName.equals(AppConsts.TIKTOK_O_PACKAGE_ID)) {
                    viewIntent.setPackage(AppConsts.TIKTOK_O_PACKAGE_ID)
                    return@stop
                }
            }
        }

        if (viewIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(viewIntent)
        } else {
            startActivity(appRouter.playStoreIntent(AppConsts.TIKTOK_M_PACKAGE_ID))
        }
    }

    private fun onSaveToGallery(videoUri: String) {
        viewModel.saveVideoToGallery(requireContext(), videoUri)
    }
}