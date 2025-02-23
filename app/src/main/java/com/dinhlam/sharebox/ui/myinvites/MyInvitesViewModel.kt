package com.dinhlam.sharebox.ui.myinvites

import androidx.lifecycle.LifecycleOwner
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyInvitesViewModel @Inject constructor(
    val boxRepository: BoxRepository,
    val realtimeDatabaseRepository: RealtimeDatabaseRepository
) : BaseViewModel<MyInvitesState>(MyInvitesState()) {

    fun listenDataChangeEvent(lifecycleOwner: LifecycleOwner) {
        realtimeDatabaseRepository.listenBoxMembersInvitedChangeEvent(lifecycleOwner) { boxInvitedDataList ->
            setState { copy(boxList = boxInvitedDataList, loading = false) }
        }
    }
}