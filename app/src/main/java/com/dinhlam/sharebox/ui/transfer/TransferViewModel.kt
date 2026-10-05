package com.dinhlam.sharebox.ui.transfer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinhlam.sharebox.data.repository.BoxTransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(private val repository: BoxTransferRepository) : ViewModel() {
    data class State(val busy: Boolean = false, val message: String? = null,
        val code: String? = null, val boxId: String? = null)
    private val mutableState = MutableStateFlow(State())
    val state = mutableState.asStateFlow()

    private fun run(operation: suspend () -> State) {
        if (mutableState.value.busy) return
        mutableState.value = State(busy = true)
        viewModelScope.launch {
            try {
                mutableState.value = operation()
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                mutableState.value = State(message = error.message ?: "Transfer failed. Please try again.")
            }
        }
    }

    fun exportLocal(boxId: String, uri: Uri) = run {
        repository.exportLocal(boxId, uri)
        State(message = "Package saved. You can share the .sharebox file with another device.")
    }
    fun exportCloud(boxId: String) = run {
        val manifest = repository.export(boxId)
        State(message = "Uploaded. Share this transfer code. Recipients can use it again to download your latest published version.", code = manifest.transferCode)
    }
    fun importLocal(uri: Uri) = run {
        val box = repository.importLocal(uri)
        State(message = "Package checked. Your local box is ready; older or identical versions are skipped.", boxId = box.boxId)
    }
    fun importCloud(code: String) = run {
        val box = repository.import(code)
        State(message = "Download checked. Your local box is ready; older or identical versions are skipped.", boxId = box.boxId)
    }
    fun keepOffline(boxId: String) = run {
        repository.keepOffline(boxId)
        State(message = "Existing cloud files are now stored on this device.", boxId = boxId)
    }
    fun makeCopy(boxId: String) = run {
        val box = repository.makeEditableCopy(boxId)
        State(message = "Your edits are saved in an independent copy. You can now import a newer version into the original box.", boxId = box.boxId)
    }
}
