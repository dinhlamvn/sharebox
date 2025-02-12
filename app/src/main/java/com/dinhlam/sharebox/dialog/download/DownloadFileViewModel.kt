package com.dinhlam.sharebox.dialog.download

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.network.DownloadServices
import com.dinhlam.sharebox.extensions.saveFile
import com.dinhlam.sharebox.model.DownloadState
import com.dinhlam.sharebox.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DownloadFileViewModel @Inject constructor(
    private val downloadServices: DownloadServices,
) : BaseViewModel<DownloadFileState>(DownloadFileState()) {

    suspend fun download(
        context: Context,
        downloadUrl: String,
        fileName: String?,
        mimeType: String?
    ) {
        if (downloadUrl.startsWith("http")) {
            downloadNetworkFile(context, downloadUrl, fileName, mimeType)
        } else if (downloadUrl.startsWith("content:")) {
            downloadLocalFile(context, downloadUrl, fileName, mimeType)
        } else {
            setState {
                copy(
                    downloadState = DownloadState.Failed(
                        IllegalStateException(
                            context.getString(
                                R.string.download_file_not_support
                            )
                        )
                    )
                )
            }
        }
    }

    private fun downloadLocalFile(
        context: Context,
        downloadUrl: String,
        fileName: String?,
        mimeType: String?
    ) {
        val downloadFileName = fileName ?: FileUtils.createFileName(
            "file",
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)!!
        )
        val downloadFile = FileUtils.createDownloadFile(downloadFileName) ?: return setState {
            copy(
                downloadState = DownloadState.Failed(IllegalStateException(context.getString(R.string.error_create_file)))
            )
        }
        context.contentResolver.openInputStream(Uri.parse(downloadUrl))?.use { inputStream ->
            downloadFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        setState { copy(downloadState = DownloadState.Finished(downloadFile)) }
    }

    private suspend fun downloadNetworkFile(
        context: Context,
        downloadUrl: String,
        fileName: String?,
        mimeType: String?
    ) {
        val downloadFileName = fileName ?: FileUtils.createFileName(
            "file",
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)!!
        )
        val downloadFile = FileUtils.createDownloadFile(downloadFileName) ?: return setState {
            copy(
                downloadState = DownloadState.Failed(IllegalStateException(context.getString(R.string.error_create_file)))
            )
        }
        downloadServices.downloadFile(downloadUrl).saveFile(downloadFile) { downloadState ->
            setState { copy(downloadState = downloadState) }
        }
    }
}