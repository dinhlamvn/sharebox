package com.dinhlam.sharebox.ui.home.videomixer

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.PagerSnapHelper
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.VideoSource
import com.dinhlam.sharebox.databinding.FragmentVideoMixerBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.queryIntentActivitiesCompat
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.ui.home.videomixer.modelview.FacebookVideoModelView
import com.dinhlam.sharebox.ui.home.videomixer.modelview.TiktokVideoModelView
import com.dinhlam.sharebox.ui.home.videomixer.modelview.YoutubeVideoModelView
import com.dinhlam.sharebox.utils.IconUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class VideoMixerFragment :
    BaseViewModelFragment<VideoMixerState, VideoMixerViewModel, FragmentVideoMixerBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    BoxSelectionDialogFragment.OnBoxSelectedListener {

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appRouter: AppRouter

    private val videoAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView("loading_video", height = ViewGroup.LayoutParams.MATCH_PARENT))
                return@getState
            }

            if (state.videos.isEmpty()) {
                add(
                    TextModelView(
                        "empty_message", getString(R.string.empty_video)
                    )
                )
                return@getState
            }

            state.videos.mapNotNull { videoMixerDetail ->
                when (videoMixerDetail.source) {
                    VideoSource.Youtube -> videoMixerDetail.sourceId.takeIfNotNullOrBlank()
                        ?.let { sourceId ->
                            YoutubeVideoModelView(
                                "video_youtube_$sourceId",
                                sourceId,
                                videoMixerDetail.shareDetail,
                                actionViewInSource = BaseListAdapter.NoHashProp(::viewInYoutube),
                                actionShareToOther = BaseListAdapter.NoHashProp(::onShareToOther),
                                actionLike = BaseListAdapter.NoHashProp(::onLike),
                                actionComment = BaseListAdapter.NoHashProp(::onComment),
                                actionBookmark = BaseListAdapter.NoHashProp(::onBookmark)
                            )
                        }

                    VideoSource.Tiktok -> videoMixerDetail.uri?.let { uri ->
                        TiktokVideoModelView(
                            "video_tiktok_$uri",
                            uri,
                            videoMixerDetail.shareDetail,
                            actionViewInSource = BaseListAdapter.NoHashProp(::viewInTiktok),
                            actionShareToOther = BaseListAdapter.NoHashProp(::onShareToOther),
                            actionLike = BaseListAdapter.NoHashProp(::onLike),
                            actionComment = BaseListAdapter.NoHashProp(::onComment),
                            actionBookmark = BaseListAdapter.NoHashProp(::onBookmark),
                            actionSaveToGallery = BaseListAdapter.NoHashProp(::onSaveToGallery)
                        )
                    }

                    VideoSource.Facebook -> videoMixerDetail.sourceId.takeIfNotNullOrBlank()
                        ?.let { sourceId ->
                            FacebookVideoModelView(
                                "video_facebook_$sourceId",
                                sourceId,
                                videoMixerDetail.shareDetail,
                                actionViewInSource = BaseListAdapter.NoHashProp(::viewInFacebook),
                                actionShareToOther = BaseListAdapter.NoHashProp(::onShareToOther),
                                actionLike = BaseListAdapter.NoHashProp(::onLike),
                                actionComment = BaseListAdapter.NoHashProp(::onComment),
                                actionBookmark = BaseListAdapter.NoHashProp(::onBookmark)
                            )
                        }
                }
            }.forEach { modelView -> add(modelView) }

            if (state.isLoadingMore) {
                add(
                    LoadingModelView(
                        "video_load_more_${state.currentPage}",
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }
    }

    private fun onSaveToGallery(videoUri: String) {
        viewModel.saveVideoToGallery(requireContext(), videoUri)
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentVideoMixerBinding {
        return FragmentVideoMixerBinding.inflate(inflater, container, false)
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = {
            return@LoadMoreLinearLayoutManager getState(viewModel) { state -> state.canLoadMore && !state.isLoadingMore }
        }) {
            viewModel.loadMores()
        }
    }

    override val viewModel: VideoMixerViewModel by viewModels()

    override fun onStateChanged(state: VideoMixerState) {
        videoAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.textTitle.setDrawableCompat(start = IconUtils.boxIcon(requireContext()) {
            copy(sizeDp = 14)
        })

        viewBinding.viewPager.layoutManager = layoutManager
        PagerSnapHelper().attachToRecyclerView(viewBinding.viewPager)
        viewBinding.viewPager.adapter = videoAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewBinding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnPullRefresh()
        }

        viewModel.consume(this, VideoMixerState::isLoadingMore) { isLoadMore ->
            layoutManager.hadTriggerLoadMore = isLoadMore
        }

        viewModel.consume(viewLifecycleOwner, VideoMixerState::currentBox) { box ->
            viewBinding.textTitle.text = box?.boxName ?: getString(R.string.box_community)
        }

        viewBinding.textTitle.setOnClickListener {
            shareHelper.showBoxSelectionDialog(childFragmentManager)
        }
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

    private fun viewInTiktok(shareData: ShareData) {
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

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val video =
            state.videos.firstOrNull { video -> video.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(video.shareDetail)
    }

    private fun onLike(shareId: String) {
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

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.bookmark(shareId, bookmarkCollectionId)
    }

    override fun onBoxSelected(boxId: String) {
        viewModel.setBox(boxId)
    }
}


