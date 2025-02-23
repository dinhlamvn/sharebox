package com.dinhlam.sharebox.ui.discover.tiktok.viewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.underline
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogFragmentTiktokDiscoverVideoViewerBinding
import com.dinhlam.sharebox.dialog.download.DownloadFileDialogFragment
import com.dinhlam.sharebox.extensions.asViewCount
import com.dinhlam.sharebox.extensions.heightPercentage
import com.dinhlam.sharebox.extensions.updateHeight
import com.dinhlam.sharebox.model.FileDownloadInfo
import com.dinhlam.sharebox.model.TiktokDiscover
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TiktokDiscoverVideoViewerDialogFragment :
    BaseDialogFragment<DialogFragmentTiktokDiscoverVideoViewerBinding>() {

    fun interface OnDialogCallback {
        fun onSave(url: String, note: String?)
    }

    companion object {
        private const val EXTRA_VIEW_TIKTOK_URL = "extra-view-tiktok-url"
        private const val EXTRA_VIEW_DESC = "extra-view-desc"
        private const val EXTRA_VIEW_COUNT = "extra-view-count"
        private const val EXTRA_LIKE_COUNT = "extra-like-count"

        @JvmStatic
        fun showDialog(
            fragmentManager: FragmentManager,
            videoUrl: String,
            tiktokDiscover: TiktokDiscover,
            dialogCallback: OnDialogCallback
        ) {
            TiktokDiscoverVideoViewerDialogFragment()
                .apply {
                    arguments = bundleOf(
                        AppExtras.EXTRA_URL to videoUrl,
                        EXTRA_VIEW_DESC to tiktokDiscover.desc,
                        EXTRA_VIEW_TIKTOK_URL to tiktokDiscover.url,
                        EXTRA_VIEW_COUNT to tiktokDiscover.playCount.toInt(),
                        EXTRA_LIKE_COUNT to tiktokDiscover.diggCount.toInt()
                    )
                    this.dialogCallback = dialogCallback
                }.show(fragmentManager, "tiktok_discover_video_viewer")
        }

    }

    override val isUseMaterialDialog: Boolean
        get() = false

    @Inject
    lateinit var router: Router

    var dialogCallback: OnDialogCallback? = null

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
        binding.root.updateHeight(heightPercentage(90))
        val tiktokUrl = arguments?.getString(EXTRA_VIEW_TIKTOK_URL) ?: return dismiss()
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
            saveVideoUrl(tiktokUrl, desc)
        }

        binding.buttonShare.setOnClickListener {
            shareVideo(tiktokUrl)
        }

        binding.viewOnTiktok.text = buildSpannedString {
            underline {
                append(getString(R.string.view_on_tiktok))
            }
        }
        binding.viewOnTiktok.setOnClickListener {
            startActivity(router.viewIntent(tiktokUrl))
        }
    }

    private fun downloadVideo(downloadUrl: String) {
        val fileName = FileUtils.createFileName("video", "mp4")
        DownloadFileDialogFragment.startDownload(
            childFragmentManager,
            FileDownloadInfo(downloadUrl, fileName, "video/mp4")
        )
    }

    private fun saveVideoUrl(url: String, desc: String?) {
        dialogCallback?.onSave(url, desc)
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