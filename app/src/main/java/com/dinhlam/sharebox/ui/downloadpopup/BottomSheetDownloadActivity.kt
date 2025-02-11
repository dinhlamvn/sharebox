package com.dinhlam.sharebox.ui.downloadpopup

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityDownloadBottomSheetBinding
import com.dinhlam.sharebox.extensions.asFileExtension
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.ext
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.DownloadHelper
import com.dinhlam.sharebox.listmodel.DownloadItemListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.utils.FileUtils
import com.dinhlam.sharebox.utils.Icons
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetDownloadActivity :
    BaseViewModelActivity<BottomSheetDownloadState, BottomSheetDownloadViewModel, ActivityDownloadBottomSheetBinding>() {

    override val viewModel: BottomSheetDownloadViewModel by viewModels()

    override fun onStateChanged(state: BottomSheetDownloadState) {
        adapter.requestBuildListModels()
    }

    private val adapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.asyncLoadDownload is BaseViewModel.AsyncLoad.Loading) {
                LoadingListModel(
                    "loading",
                    height = 200.dp,
                    message = getString(R.string.processing)
                ).attachTo(
                    this
                )
                return@getState
            }
            val videos = state.asyncLoadDownload.data?.videos.orEmpty()
            val audios = state.asyncLoadDownload.data?.audios.orEmpty()
            val images = state.asyncLoadDownload.data?.images.orEmpty()
            val files = state.asyncLoadDownload.data?.files.orEmpty()

            if (videos.isEmpty() && audios.isEmpty() && images.isEmpty() && files.isEmpty()) {
                return@getState TextListModel(
                    "empty",
                    getString(R.string.nothing_to_download),
                    textAppearance = R.style.TextBodyMedium,
                    height = 200.dp,
                    gravity = Gravity.CENTER
                ).attachTo(this)
            }

            if (videos.isNotEmpty()) {
                TextListModel(
                    "title_video",
                    getString(R.string.download_videos),
                    textAppearance = R.style.TextTitleMedium,
                    height = 50.dp(),
                    gravity = Gravity.START
                ).attachTo(this)

                videos.forEachIndexed { index, downloadData ->
                    DownloadItemListModel(
                        "download_video_$index",
                        Icons.MP4_LOGO,
                        "${
                            getString(
                                R.string.download_mimetype,
                                downloadData.mimeType.asFileExtension()
                            )
                        } ${downloadData.suffix}",
                        BaseListAdapter.NoHashProp(View.OnClickListener {
                            downloadVideo(downloadData.mimeType, downloadData.downloadUrl)
                        })
                    ).attachTo(this)
                    VerticalDividerListModel("video_divider_$index", height = 1.dp()).attachTo(this)
                }

                VerticalDividerListModel(
                    "video_spacing",
                    height = 16.dp(),
                    dividerColor = android.R.color.transparent
                ).attachTo(this)
            }

            if (audios.isNotEmpty()) {
                TextListModel(
                    "title_audio",
                    getString(R.string.download_audios),
                    textAppearance = R.style.TextTitleMedium,
                    height = 50.dp(),
                    gravity = Gravity.START
                ).attachTo(this)

                audios.forEachIndexed { index, downloadData ->
                    DownloadItemListModel(
                        "download_audio_$index",
                        Icons.MP3_LOGO,
                        "${
                            getString(
                                R.string.download_mimetype,
                                downloadData.mimeType.asFileExtension()
                            )
                        } ${downloadData.suffix}",
                        actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                            downloadAudio(downloadData.mimeType, downloadData.downloadUrl)
                        })
                    ).attachTo(this)
                    VerticalDividerListModel("audio_divider_$index", height = 1.dp()).attachTo(this)
                }

                VerticalDividerListModel(
                    "audio_spacing",
                    height = 16.dp(),
                    dividerColor = android.R.color.transparent
                ).attachTo(this)
            }

            if (images.isNotEmpty()) {
                TextListModel(
                    "title_image",
                    getString(R.string.download_images),
                    textAppearance = R.style.TextTitleMedium,
                    height = 50.dp(),
                    gravity = Gravity.START
                ).attachTo(this)

                images.forEachIndexed { index, downloadData ->
                    DownloadItemListModel(
                        "download_image_$index",
                        downloadData.downloadUrl,
                        "${
                            getString(
                                R.string.download_mimetype,
                                downloadData.mimeType.asFileExtension()
                            )
                        } ${downloadData.suffix}",
                        actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                            downloadImage(downloadData.mimeType, downloadData.downloadUrl)
                        })
                    ).attachTo(this)
                    VerticalDividerListModel("image_divider_$index", height = 1.dp()).attachTo(this)
                }
            }

            if (files.isNotEmpty()) {
                TextListModel(
                    "title_files",
                    getString(R.string.download_files),
                    textAppearance = R.style.TextTitleMedium,
                    height = 50.dp(),
                    gravity = Gravity.START
                ).attachTo(this)

                files.forEachIndexed { index, downloadData ->
                    DownloadItemListModel(
                        "download_file_$index",
                        Icons.FILE_LOGO,
                        "${
                            getString(
                                R.string.download_mimetype,
                                downloadData.mimeType.asFileExtension()
                            )
                        } ${downloadData.suffix}",
                        actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                            downloadFile(downloadData.mimeType, downloadData.downloadUrl)
                        })
                    ).attachTo(this)
                    VerticalDividerListModel("file_divider_$index", height = 1.dp()).attachTo(this)
                }
            }
        }
    }

    private fun downloadVideo(mimeType: String, downloadUrl: String) {
        val outputFile = FileUtils.createFileName("video", mimeType.asFileExtension())
        DownloadHelper.enqueueDownload(this, downloadUrl, outputFile)
    }

    private fun downloadAudio(mimeType: String, downloadUrl: String) {
        val outputFile = FileUtils.createFileName("audio", mimeType.asFileExtension())
        DownloadHelper.enqueueDownload(this, downloadUrl, outputFile)
    }

    private fun downloadImage(mimeType: String, url: String) {
        val outputFile = FileUtils.createFileName("image", mimeType.asFileExtension())
        DownloadHelper.enqueueDownload(this, url, outputFile)
    }

    private fun downloadFile(mimeType: String, downloadUrl: String) {
        val outputFile = FileUtils.createFileName("file", mimeType.asFileExtension())
        DownloadHelper.enqueueDownload(this, downloadUrl, outputFile)
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                finishAndRemoveTask()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateViewBinding(): ActivityDownloadBottomSheetBinding {
        return ActivityDownloadBottomSheetBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.background.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        registerOnBackPressHandler {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.recyclerView.adapter = adapter
        bottomSheetBehavior = BottomSheetBehavior.from(binding.container)
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        handleShareData()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareData()
    }

    private fun handleShareData() {
        val (action, type) = intent.action to intent.type
        when {
            action == Intent.ACTION_SEND && type?.startsWith("text/") == true -> {
                val shareContent = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                handleShareData(shareContent)
            }

            else -> {
                val urls = intent.getStringArrayListExtra(AppExtras.EXTRA_URLS).orEmpty()
                if (urls.isEmpty()) {
                    showToast(R.string.nothing_to_download)
                    if (isTaskRoot) {
                        finishAndRemoveTask()
                    } else {
                        finish()
                    }
                }
                viewModel.download(urls)
            }
        }
    }

    private fun handleShareData(text: String) {
        if (!text.isWebLink()) {
            showToast(R.string.no_support_download_this_content)
            return if (isTaskRoot) {
                finishAndRemoveTask()
            } else {
                finish()
            }
        }
        viewModel.download(listOf(text))
    }
}