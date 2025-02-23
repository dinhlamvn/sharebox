package com.dinhlam.sharebox.ui.myinvites.listing

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.mapper.ShareToShareDetailMapper
import com.dinhlam.sharebox.data.realtime.RealtimeDatabaseRepository
import com.dinhlam.sharebox.extensions.getNonNull
import com.dinhlam.sharebox.model.ShareDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyInviteShareListingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository,
    private val shareDetailMapper: ShareToShareDetailMapper,
) : BaseViewModel<MyInviteShareListingState>(MyInviteShareListingState()) {

    fun listen(lifecycleOwner: LifecycleOwner) {
        realtimeDatabaseRepository.listenInvitedBoxShareListingChangeEvent(
            lifecycleOwner,
            savedStateHandle.getNonNull(AppExtras.EXTRA_BOX_ID)
        ) { shares ->
            val shareDetailList =
                shares.map(shareDetailMapper::map).sortedByDescending(ShareDetail::createdAt)
            setState { copy(shares = shareDetailList, isLoading = false) }
        }
    }
}