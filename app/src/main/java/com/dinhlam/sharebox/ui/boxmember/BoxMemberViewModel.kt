package com.dinhlam.sharebox.ui.boxmember

import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.RealtimeDatabaseRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.model.BoxMember
import com.dinhlam.sharebox.utils.UserUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxMemberViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val realtimeDatabaseRepository: RealtimeDatabaseRepository
) : BaseViewModel<BoxMemberState>(BoxMemberState(savedStateHandle.getNonNull(AppExtras.EXTRA_BOX_ID))) {

    init {
        onChange(BoxMemberState::boxId) { boxId ->
            realtimeDatabaseRepository.onBoxMembersChange(boxId) { list ->
                setState { copy(members = list) }
            }
        }
    }

    fun addMember(email: String) = getState { state ->
        val memberId = UserUtils.createUserId(email)
        doInBackground {
            realtimeDatabaseRepository.pushBoxMember(state.boxId, memberId, email)
        }
    }

    fun removeMember(member: BoxMember) = getState { state ->
        doInBackground {
            realtimeDatabaseRepository.removeBoxMember(state.boxId, member.dataKey)
        }
    }
}