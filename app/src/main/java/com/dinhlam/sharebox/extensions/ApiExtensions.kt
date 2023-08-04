package com.dinhlam.sharebox.extensions

import com.dinhlam.sharebox.model.DownloadState
import okhttp3.ResponseBody
import java.io.File

suspend fun ResponseBody.saveFile(
    dest: File,
    contentLength: Long = 0L,
    callback: suspend (DownloadState) -> Unit
) {
    callback(DownloadState.Downloading(0))
    try {
        byteStream().use { inputSteam ->
            dest.outputStream().use { outputStream ->
                val totalBytes = contentLength.takeIf { it > 0 } ?: contentLength()
                val buffer = ByteArray(1024 * 1024)
                var bytesProgressed = 0L
                var byes = inputSteam.read(buffer)

                while (byes > 0) {
                    outputStream.write(buffer, 0, byes)
                    bytesProgressed += byes
                    byes = inputSteam.read(buffer)
                    callback(
                        DownloadState.Downloading(
                            bytesProgressed.times(100).div(totalBytes).toInt()
                        )
                    )
                }
            }
        }
        callback(DownloadState.Finished)
    } catch (e: Exception) {
        callback(DownloadState.Failed(e))
    }
}