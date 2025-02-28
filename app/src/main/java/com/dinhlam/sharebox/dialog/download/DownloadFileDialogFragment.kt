package com.dinhlam.sharebox.dialog.download

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogFragmentDownloadFileBinding
import com.dinhlam.sharebox.extensions.asHumanReadableSize
import com.dinhlam.sharebox.extensions.getMimeTypeFromUri
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.DownloadState
import com.dinhlam.sharebox.model.FileDownloadInfo
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DownloadFileDialogFragment :
    BaseViewModelDialogFragment<DownloadFileState, DownloadFileViewModel, DialogFragmentDownloadFileBinding>() {

    companion object {
        @JvmStatic
        fun startDownload(
            fragmentManager: FragmentManager,
            downloadInfo: FileDownloadInfo
        ) {
            val dialogFragment = DownloadFileDialogFragment()
            dialogFragment.arguments =
                bundleOf(AppExtras.EXTRA_DATA to downloadInfo)
            dialogFragment.show(fragmentManager, "download_file_dialog")
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentDownloadFileBinding {
        return DialogFragmentDownloadFileBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var router: Router

    override val viewModel: DownloadFileViewModel by viewModels()

    override fun onStateChanged(state: DownloadFileState) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val downloadInfo =
            arguments?.getParcelableExtraCompat<FileDownloadInfo>(AppExtras.EXTRA_DATA)
                ?: return dismiss()

        onChange(DownloadFileState::downloadState) { downloadState ->
            when (downloadState) {
                is DownloadState.Finished -> {
                    val fileUri =
                        FileUtils.getUriFromFile(requireContext(), downloadState.downloadFile)
                    val mimeType = downloadInfo.mimeType ?: context?.getMimeTypeFromUri(fileUri)
                    val intent =
                        router.shareToOtherIntent(requireContext(), fileUri, mimeType)?.apply {
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                    if (intent != null) {
                        startActivity(intent)
                    }
                    MediaScannerConnection.scanFile(
                        requireContext(),
                        arrayOf(downloadState.downloadFile.path),
                        arrayOf(mimeType)
                    ) { path, uri ->
                        Logger.debug("Media scan is completed: $path - $uri")
                    }
                    showToast(getString(R.string.downloaded, downloadState.downloadFile.path))
                    dismiss()
                }

                is DownloadState.Failed -> {
                    showToast(downloadState.error?.message)
                    dismiss()
                }

                is DownloadState.Downloading -> {
                    if (downloadState.progress > 0) {
                        binding.textMessage.text = getString(R.string.downloading)
                        binding.textProgress.text =
                            getString(R.string.percentage, downloadState.progress)
                        binding.progressBar.progress = downloadState.progress
                    } else if (downloadState.totalBytesDownloaded > 0L) {
                        binding.textMessage.text = getString(R.string.downloading)
                        binding.progressBar.progress = 0
                        binding.textProgress.text = getString(
                            R.string.downloaded,
                            downloadState.totalBytesDownloaded.asHumanReadableSize()
                        )
                    } else {
                        binding.textMessage.text = getString(R.string.processing)
                        binding.progressBar.progress = 0
                        binding.textProgress.text = null
                    }
                }
            }
        }

        viewModel.download(
            requireContext(),
            downloadInfo.downloadUrl,
            downloadInfo.fileName,
        )
    }
}