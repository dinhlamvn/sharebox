package com.dinhlam.sharebox.ui.home.videomixer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.PagerSnapHelper
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.data.model.VideoSource
import com.dinhlam.sharebox.databinding.FragmentVideoMixerBinding
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.services.VideoMixerService
import com.dinhlam.sharebox.ui.home.videomixer.modelview.FacebookVideoModelView
import com.dinhlam.sharebox.ui.home.videomixer.modelview.TiktokVideoModelView
import com.dinhlam.sharebox.ui.home.videomixer.modelview.YoutubeVideoModelView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class VideoMixerFragment :
    BaseViewModelFragment<VideoMixerState, VideoMixerViewModel, FragmentVideoMixerBinding>() {

    private val serviceConnection = object : ServiceConnection {

        private var bound = false

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            bound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            bound = false
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    private val videoAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView("loading_video"))
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
                                actionShareToOther = BaseListAdapter.NoHashProp(::onShareToOther),
                                actionVote = BaseListAdapter.NoHashProp(::onVote),
                                actionComment = BaseListAdapter.NoHashProp(::onComment),
                                actionBookmark = BaseListAdapter.NoHashProp(::onBookmark)
                            )
                        }

                    VideoSource.Tiktok -> videoMixerDetail.uri?.let { uri ->
                        TiktokVideoModelView(
                            "video_tiktok_$uri",
                            uri,
                            videoMixerDetail.shareDetail,
                            actionShareToOther = BaseListAdapter.NoHashProp(::onShareToOther),
                            actionVote = BaseListAdapter.NoHashProp(::onVote),
                            actionComment = BaseListAdapter.NoHashProp(::onComment),
                            actionBookmark = BaseListAdapter.NoHashProp(::onBookmark)
                        )
                    }

                    VideoSource.Facebook -> videoMixerDetail.sourceId.takeIfNotNullOrBlank()
                        ?.let { sourceId ->
                            FacebookVideoModelView(
                                "video_facebook_$sourceId",
                                sourceId,
                                videoMixerDetail.shareDetail,
                                actionShareToOther = BaseListAdapter.NoHashProp(::onShareToOther),
                                actionVote = BaseListAdapter.NoHashProp(::onVote),
                                actionComment = BaseListAdapter.NoHashProp(::onComment),
                                actionBookmark = BaseListAdapter.NoHashProp(::onBookmark)
                            )
                        }

                    else -> null
                }
            }.forEach { modelView -> add(modelView) }
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentVideoMixerBinding {
        return FragmentVideoMixerBinding.inflate(inflater, container, false)
    }

    override val viewModel: VideoMixerViewModel by viewModels()

    override fun onStateChanged(state: VideoMixerState) {
        videoAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PagerSnapHelper().attachToRecyclerView(viewBinding.viewPager)
        viewBinding.viewPager.adapter = videoAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewBinding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnPullRefresh()
        }
    }

    override fun onStart() {
        super.onStart()
        context?.bindService(
            Intent(requireContext(), VideoMixerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        context?.unbindService(serviceConnection)
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val video =
            state.videos.firstOrNull { video -> video.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(video.shareDetail)
    }

    private fun onVote(shareId: String) {
        viewModel.vote(shareId)
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPicker(requireActivity(), collectionId) { pickedId ->
                viewModel.bookmark(shareId, pickedId)
            }
        }
    }

    private fun onComment(shareId: String) {
        shareHelper.showComment(childFragmentManager, shareId)
    }
}