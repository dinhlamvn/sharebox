package com.dinhlam.sharebox.dialog.download

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
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.model.DownloadState
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadFileDialogFragment :
    BaseViewModelDialogFragment<DownloadFileState, DownloadFileViewModel, DialogFragmentDownloadFileBinding>() {

    companion object {
        @JvmStatic
        fun showDialog(
            fragmentManager: FragmentManager,
            downloadUrl: String,
            fileName: String?,
            mimeType: String?
        ) {
            val dialogFragment = DownloadFileDialogFragment()
            dialogFragment.arguments =
                bundleOf(
                    AppExtras.EXTRA_URL to downloadUrl,
                    AppExtras.EXTRA_NAME to fileName,
                    AppExtras.EXTRA_MIMETYPE to mimeType
                )
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

    private var downloadJob: Job? = null

    override fun onStateChanged(state: DownloadFileState) {
        if (state.downloadState is DownloadState.Downloading) {
            if (state.downloadState.progress > 0) {
                binding.textProgress.text =
                    getString(R.string.percentage, state.downloadState.progress)
                binding.progressBar.progress = state.downloadState.progress
            } else {
                binding.progressBar.progress = 100
                binding.textProgress.text = getString(
                    R.string.downloaded,
                    state.downloadState.totalBytesDownloaded.asHumanReadableSize()
                )
            }
        } else {
            binding.progressBar.progress = 0
            binding.textProgress.text = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onChange(DownloadFileState::downloadState) { downloadState ->
            when (downloadState) {
                is DownloadState.Finished -> {
                    val fileUri =
                        FileUtils.getUriFromFile(requireContext(), downloadState.downloadFile)
                    val mimeType = context?.getMimeTypeFromUri(fileUri) ?: return@onChange dismiss()
                    val intent = router.shareToOtherIntent(requireContext(), fileUri, mimeType)
                    if (intent != null) {
                        startActivity(intent)
                    }
                    dismiss()
                }

                is DownloadState.Failed -> {
                    showToast(downloadState.error?.message)
                    dismiss()
                }

                else -> {}
            }
        }

        val downloadUrl = arguments?.getString(AppExtras.EXTRA_URL) ?: return dismiss()
        val fileName = arguments?.getString(AppExtras.EXTRA_NAME)
        val mimeType = arguments?.getString(AppExtras.EXTRA_MIMETYPE)

        downloadJob = fragmentScope.launch(Dispatchers.IO) {
            viewModel.download(requireContext(), downloadUrl, fileName, mimeType)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (downloadJob?.isActive == true && downloadJob?.isCompleted == false) {
            downloadJob?.cancel()
        }
    }
}