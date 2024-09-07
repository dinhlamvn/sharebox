package com.dinhlam.sharebox.ui.boxinvited

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.model.BoxDetail
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxInvitedViewModel @Inject constructor(
    val boxRepository: BoxRepository,
    val realtimeDatabaseRepository: RealtimeDatabaseRepository
) : BaseViewModel<BoxInvitedState>(BoxInvitedState()) {

    private val listener: ValueEventListener =
        realtimeDatabaseRepository.onBoxMemberInvitedChange { list ->
            suspend {
                boxRepository.find(list)
            }.execute { asyncLoad ->
                copy(boxList = asyncLoad.data.orEmpty(), loading = false)
            }
        }

    override fun onCleared() {
        super.onCleared()
        realtimeDatabaseRepository.removeBoxMembersInvitedChangeEvent(listener)
    }
}