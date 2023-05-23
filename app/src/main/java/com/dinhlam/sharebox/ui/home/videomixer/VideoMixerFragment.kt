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
import com.dinhlam.sharebox.databinding.FragmentVideoMixerBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.services.VideoMixerService
import com.dinhlam.sharebox.ui.home.videomixer.modelview.YoutubeVideoModelView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VideoMixerFragment :
    BaseViewModelFragment<VideoMixerState, VideoMixerViewModel, FragmentVideoMixerBinding>() {

    private val serviceConnection = object : ServiceConnection {

        private var bound = false

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            binder.cast<VideoMixerService.LocalBinder>()?.getService()?.startMixVideoFromShareData()
            bound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            bound = false
        }
    }


    private val videoAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView("loading_video"))
                return@getState
            }

            state.videos.forEach { videoMixerDetail ->
                add(
                    YoutubeVideoModelView(
                        "video_youtube_${videoMixerDetail.id}",
                        videoMixerDetail.sourceId,
                        videoMixerDetail.shareDetail
                    )
                )
            }
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
}