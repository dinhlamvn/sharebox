package com.dinhlam.sharebox.ui.boxmember

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.model.BoxMember
import com.dinhlam.sharebox.utils.UserUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BoxMemberViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val boxRepository: BoxRepository,
    val realtimeDatabaseRepository: RealtimeDatabaseRepository
) : BaseViewModel<BoxMemberState>(BoxMemberState()) {

    private val boxId: String = savedStateHandle.getNonNull(AppExtras.EXTRA_BOX_ID)

    init {
        loadBoxDetail(boxId)
    }

    private fun loadBoxDetail(boxId: String) {
        suspend {
            boxRepository.findOne(boxId)!!
        }.execute { asyncLoad ->
            val box = asyncLoad.data
            copy(boxDetail = box)
        }
    }

    fun addMember(email: String) = doInBackground {
        val boxDetail = boxRepository.findOne(boxId)!!
        realtimeDatabaseRepository.pushBoxMember(
            boxDetail.boxId,
            UserUtils.createUserId(email),
            email
        )
    }

    fun removeMember(member: BoxMember) = doInBackground {
        realtimeDatabaseRepository.removeBoxMember(boxId, member.memberId)
    }

    fun listenDataChangeEvent(lifecycleOwner: LifecycleOwner) {
        realtimeDatabaseRepository.listenBoxMembersChangeEvent(
            lifecycleOwner,
            boxId
        ) { memberList ->
            setState { copy(members = memberList, loading = false) }
        }
    }
}