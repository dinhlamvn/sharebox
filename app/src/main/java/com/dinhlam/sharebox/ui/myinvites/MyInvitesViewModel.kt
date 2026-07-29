package com.dinhlam.sharebox.ui.myinvites

import androidx.lifecycle.LifecycleOwner
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.BoxTransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyInvitesViewModel @Inject constructor(
    val boxRepository: BoxRepository,
    val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val boxTransferRepository: BoxTransferRepository,
) : BaseViewModel<MyInvitesState>(MyInvitesState()) {

    fun listenDataChangeEvent(lifecycleOwner: LifecycleOwner) {
        setState { copy(loading = true) }
        realtimeDatabaseRepository.listenBoxMembersInvitedChangeEvent(
            lifecycleOwner,
            block = { boxInvitedDataList ->
                setState { copy(boxList = boxInvitedDataList, loading = false) }
            },
            onError = {
                setState { copy(loading = false) }
            }
        )
    }

    fun importBox(boxId: String) {
        suspend { boxTransferRepository.import(boxId) }.execute { result ->
            copy(importBox = result)
        }
    }

    fun exportBox(boxId: String) {
        suspend { boxTransferRepository.export(boxId) }.execute { result ->
            copy(exportBox = result)
        }
    }

    fun consumeImportResult() {
        setState { copy(importBox = AsyncLoad.UnInitialized) }
    }

    fun consumeExportResult() {
        setState { copy(exportBox = AsyncLoad.UnInitialized) }
    }
}
