package com.dinhlam.sharebox.ui.boxmember

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.model.BoxMember
import com.dinhlam.sharebox.utils.UserUtils
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxMemberViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val boxRepository: BoxRepository,
    val realtimeDatabaseRepository: RealtimeDatabaseRepository
) : BaseViewModel<BoxMemberState>(BoxMemberState(savedStateHandle.getNonNull(AppExtras.EXTRA_BOX_ID))) {

    private var listener: ValueEventListener =
        realtimeDatabaseRepository.onBoxMembersChange(savedStateHandle.getNonNull(AppExtras.EXTRA_BOX_ID)) { list ->
            setState { copy(members = list, loading = false) }
        }

    fun addMember(email: String) = getState { state ->
        val memberId = UserUtils.createUserId(email)
        doInBackground {
            val boxDetail = boxRepository.findOne(state.boxId)!!
            realtimeDatabaseRepository.pushBoxMember(
                boxDetail.boxId,
                memberId,
                email
            )
        }
    }

    fun removeMember(member: BoxMember) = getState { state ->
        doInBackground {
            realtimeDatabaseRepository.removeBoxMember(state.boxId, member.dataKey, member.memberId)
        }
    }

    override fun onCleared() {
        getState { state ->
            realtimeDatabaseRepository.removeBoxMembersChangeEvent(state.boxId, listener)
        }
        super.onCleared()
    }
}