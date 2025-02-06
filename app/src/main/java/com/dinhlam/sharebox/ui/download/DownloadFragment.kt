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
import com.dinhlam.sharebox.extensions.asFileExtension
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.DownloadHelper
import com.dinhlam.sharebox.listmodel.DownloadItemListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadFragment :
    BaseViewModelFragment<DownloadState, DownloadViewModel, FragmentDownloadBinding>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDownloadBinding {
        return FragmentDownloadBinding.inflate(layoutInflater, container, false)
    }

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

            if (videos.isEmpty() && audios.isEmpty() && images.isEmpty()) {
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
                        downloadData.downloadUrl,
                        "${
                            getString(
                                R.string.download_mimetype,
                                downloadData.mimeType
                            )
                        } ${downloadData.suffix}",
                        true,
                        BaseListAdapter.NoHashProp(View.OnClickListener {
                            downloadVideo(
                                downloadData.id,
                                downloadData.mimeType,
                                downloadData.downloadUrl
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
                        "${
                            getString(
                                R.string.download_mimetype,
                                downloadData.mimeType
                            )
                        } ${downloadData.suffix}",
                        false,
                        actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                            downloadAudio(
                                downloadData.id,
                                downloadData.mimeType,
                                downloadData.downloadUrl
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
                        "${
                            getString(
                                R.string.download_mimetype,
                                downloadData.mimeType
                            )
                        } ${downloadData.suffix}",
                        false,
                        actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                            downloadImages(
                                downloadData.id,
                                listOf(downloadData.downloadUrl)
                            )
                        })
                    ).attachTo(this)
                    VerticalDividerListModel("image_divider_$index", height = 1.dp()).attachTo(this)
                }
            }
        }
    }

    private fun downloadVideo(id: String, mimeType: String, downloadUrl: String) {
        val outputFile =
            "sharebox_video_${id}_${System.currentTimeMillis()}.${mimeType.asFileExtension()}"
        DownloadHelper.enqueueDownload(requireContext(), downloadUrl, outputFile)
    }

    private fun downloadAudio(id: String, mimeType: String, downloadUrl: String) {
        val outputFile =
            "sharebox_audio_${id}_${System.currentTimeMillis()}.${mimeType.asFileExtension()}"
        DownloadHelper.enqueueDownload(requireContext(), downloadUrl, outputFile)
    }

    private fun downloadImages(id: String, urls: List<String>) {
        DownloadHelper.downloadImages(requireContext(), id, urls)
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