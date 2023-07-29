package com.dinhlam.sharebox.extensions

import com.dinhlam.sharebox.model.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import java.io.File

fun ResponseBody.saveFile(dest: File): Flow<DownloadState> {
    return flow {
        emit(DownloadState.Downloading(0))
        try {
            byteStream().use { inputSteam ->
                dest.outputStream().use { outputStream ->
                    val totalBytes = contentLength()
                    val buffer = ByteArray(1024 * 1024)
                    var bytesProgressed = 0L
                    var byes = inputSteam.read(buffer)

                    while (byes > 0) {
                        outputStream.write(buffer, 0, byes)
                        bytesProgressed += byes
                        byes = inputSteam.read(buffer)
                        emit(
                            DownloadState.Downloading(
                                bytesProgressed.times(100).div(totalBytes).toInt()
                            )
                        )
                    }
                }
            }
            emit(DownloadState.Finished)
        } catch (e: Exception) {
            emit(DownloadState.Failed(e))
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()
}