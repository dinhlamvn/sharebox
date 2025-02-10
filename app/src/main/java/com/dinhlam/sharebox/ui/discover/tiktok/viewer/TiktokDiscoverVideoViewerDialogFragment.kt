package com.dinhlam.sharebox.ui.discover.tiktok.viewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.underline
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogFragmentTiktokDiscoverVideoViewerBinding
import com.dinhlam.sharebox.extensions.asViewCount
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.heightPercentage
import com.dinhlam.sharebox.extensions.updateHeight
import com.dinhlam.sharebox.helper.DownloadHelper
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TiktokDiscoverVideoViewerDialogFragment :
    BaseDialogFragment<DialogFragmentTiktokDiscoverVideoViewerBinding>() {

    fun interface OnDialogCallback {
        fun onSave(url: String, note: String?)
    }

    companion object {
        const val EXTRA_VIEW_TIKTOK_URL = "extra-view-tiktok-url"
        const val EXTRA_VIEW_DESC = "extra-view-desc"
        const val EXTRA_VIEW_COUNT = "extra-view-count"
        const val EXTRA_LIKE_COUNT = "extra-like-count"
    }

    override val isUseMaterialDialog: Boolean
        get() = false

    @Inject
    lateinit var router: Router

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == ExoPlayer.STATE_READY) {
                binding.progressBar.isVisible = false
            }
        }
    }

    private val player by lazy {
        ExoPlayer.Builder(requireContext())
            .build()
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentTiktokDiscoverVideoViewerBinding {
        return DialogFragmentTiktokDiscoverVideoViewerBinding.inflate(
            layoutInflater,
            container,
            false
        )
    }

    override fun onPause() {
        super.onPause()
        binding.videoView.onPause()
        if (player.isPlaying) {
            player.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.videoView.onResume()
        if (!player.isPlaying) {
            player.play()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
        player.removeListener(playerListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.videoBackground.updateHeight(heightPercentage(80))
        val videoUrl = arguments?.getString(AppExtras.EXTRA_URL) ?: return dismiss()
        binding.videoView.player = player
        player.addListener(playerListener)
        player.repeatMode = ExoPlayer.REPEAT_MODE_ALL

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        val desc = arguments?.getString(EXTRA_VIEW_DESC)
        binding.textDesc.text = desc

        val viewCount = arguments?.getInt(EXTRA_VIEW_COUNT) ?: 0
        val likeCount = arguments?.getInt(EXTRA_LIKE_COUNT) ?: 0

        binding.textViewCount.text = viewCount.asViewCount()
        binding.textLikeCount.text = likeCount.asViewCount()

        binding.buttonDownload.setOnClickListener {
            downloadVideo(videoUrl)
        }

        binding.buttonSave.setOnClickListener {
            saveVideoUrl(videoUrl, desc)
        }

        binding.buttonShare.setOnClickListener {
            val url = arguments?.getString(EXTRA_VIEW_TIKTOK_URL) ?: return@setOnClickListener
            shareVideo(url)
        }

        binding.viewOnTiktok.text = buildSpannedString {
            underline {
                append(getString(R.string.view_on_tiktok))
            }
        }
        binding.viewOnTiktok.setOnClickListener {
            val url = arguments?.getString(EXTRA_VIEW_TIKTOK_URL) ?: return@setOnClickListener
            startActivity(router.viewIntent(url))
        }
    }

    private fun downloadVideo(downloadUrl: String) {
        val outputFile = "sharebox_video_${System.currentTimeMillis()}.mp4"
        DownloadHelper.enqueueDownload(requireContext(), downloadUrl, outputFile)
    }

    private fun saveVideoUrl(url: String, desc: String?) {
        parentFragment?.cast<OnDialogCallback>()?.onSave(url, desc)
    }

    private fun shareVideo(url: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, url)
        intent.type = "text/*"
        val chooser = Intent.createChooser(intent, "Share To")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(chooser)
    }
}