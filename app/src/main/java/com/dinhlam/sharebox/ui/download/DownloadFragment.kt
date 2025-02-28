package com.dinhlam.sharebox.ui.download

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.databinding.FragmentDownloadBinding
import com.dinhlam.sharebox.dialog.download.DownloadFileDialogFragment
import com.dinhlam.sharebox.extensions.asFileExtension
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.AppSettingHelper
import com.dinhlam.sharebox.helper.NetworkHelper
import com.dinhlam.sharebox.listmodel.DownloadItemListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.AppSettings
import com.dinhlam.sharebox.model.FileDownloadInfo
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.FileUtils
import com.dinhlam.sharebox.utils.Icons
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DownloadFragment :
    BaseViewModelFragment<DownloadState, DownloadViewModel, FragmentDownloadBinding>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDownloadBinding {
        return FragmentDownloadBinding.inflate(layoutInflater, container, false)
    }

    @Inject
    lateinit var appSettingHelper: AppSettingHelper

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var router: Router

    override val viewModel: DownloadViewModel by viewModels()

    private val adapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.asyncLoadDownload is BaseViewModel.AsyncLoad.Loading) {
                LoadingListModel(
                    "loading",
                    height = ViewGroup.LayoutParams.MATCH_PARENT,
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
                        "Download Video - ${downloadData.suffix}",
                        BaseListAdapter.NoHashProp(View.OnClickListener {
                            download(
                                downloadData.mimeType,
                                downloadData.downloadUrl,
                                1
                            )
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
                        "Download Audio - ${downloadData.suffix}",
                        actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                            download(
                                downloadData.mimeType,
                                downloadData.downloadUrl,
                                2
                            )
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
                        "Download Image - ${downloadData.suffix}",
                        actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                            download(downloadData.mimeType, downloadData.downloadUrl, 3)
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
                        "Download File - ${downloadData.suffix}",
                        actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                            download(downloadData.mimeType, downloadData.downloadUrl, 4)
                        })
                    ).attachTo(this)
                    VerticalDividerListModel("file_divider_$index", height = 1.dp()).attachTo(this)
                }
            }
        }
    }

    private fun download(mimeType: String, downloadUrl: String, type: Int) {
        if (appSettingHelper.getNetworkCondition() == AppSettings.NetworkCondition.WIFI_ONLY && !networkHelper.isNetworkWifiConnected()) {
            return MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.network_wifi_only_warning)
                .setPositiveButton(R.string.dialog_ok) { _, _ ->
                    when (type) {
                        1 -> downloadVideo(mimeType, downloadUrl)
                        2 -> downloadAudio(mimeType, downloadUrl)
                        3 -> downloadImage(mimeType, downloadUrl)
                        4 -> downloadFile(mimeType, downloadUrl)
                    }
                }.setNegativeButton(R.string.dialog_cancel, null)
                .create()
                .show()
        }
        when (type) {
            1 -> downloadVideo(mimeType, downloadUrl)
            2 -> downloadAudio(mimeType, downloadUrl)
            3 -> downloadImage(mimeType, downloadUrl)
            4 -> downloadFile(mimeType, downloadUrl)
        }
    }

    private fun downloadVideo(mimeType: String, downloadUrl: String) {
        val fileName = FileUtils.createFileName("video", mimeType.asFileExtension())
        DownloadFileDialogFragment.startDownload(
            childFragmentManager,
            FileDownloadInfo(downloadUrl, fileName, mimeType)
        )
    }

    private fun downloadAudio(mimeType: String, downloadUrl: String) {
        val fileName = FileUtils.createFileName("audio", mimeType.asFileExtension())
        DownloadFileDialogFragment.startDownload(
            childFragmentManager,
            FileDownloadInfo(downloadUrl, fileName, mimeType)
        )
    }

    private fun downloadImage(mimeType: String, downloadUrl: String) {
        val fileName = FileUtils.createFileName("image", mimeType.asFileExtension())
        DownloadFileDialogFragment.startDownload(
            childFragmentManager,
            FileDownloadInfo(downloadUrl, fileName, mimeType)
        )
    }

    private fun downloadFile(mimeType: String, downloadUrl: String) {
        val fileName = FileUtils.createFileName("file", mimeType.asFileExtension())
        DownloadFileDialogFragment.startDownload(
            childFragmentManager,
            FileDownloadInfo(downloadUrl, fileName, mimeType)
        )
    }

    override fun onStateChanged(state: DownloadState) {
        adapter.requestBuildListModels()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.attachTo(binding.recyclerView, viewLifecycleOwner)

        binding.buttonPaste.setOnClickListener {
            val clipboardData = pickWebLinkFromClipboard()?.toString() ?: return@setOnClickListener
            binding.editLink.setText(clipboardData)
        }

        binding.buttonDownload.setOnClickListener {
            binding.editLink.hideKeyboard()
            val correctLink =
                getCorrectLink().takeIfNotNullOrBlank() ?: return@setOnClickListener showToast(
                    getString(R.string.require_input_link)
                )

            if (!correctLink.isWebLink()) {
                return@setOnClickListener showToast(getString(R.string.require_input_link))
            }
            viewModel.download(correctLink)
        }
    }

    private fun getCorrectLink(): String {
        val link = binding.editLink.getTrimmedText().takeIfNotNullOrBlank() ?: return ""
        return if (link.startsWith("http://") || link.startsWith("https://")) {
            link
        } else {
            "https://$link"
        }
    }

    private fun pickWebLinkFromClipboard(): Uri? {
        val clipboardManager =
            context?.getSystemServiceCompat<ClipboardManager>(Context.CLIPBOARD_SERVICE)
                ?: return null
        if (!clipboardManager.hasPrimaryClip()) {
            return null
        }
        val clipItemCount = clipboardManager.primaryClip?.itemCount ?: 0
        if (clipItemCount == 0) {
            return null
        }
        val text = clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
            ?: return null

        if (text.isWebLink()) {
            return Uri.parse(text)
        }

        return null
    }
}