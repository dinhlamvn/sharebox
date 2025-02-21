package com.dinhlam.sharebox.ui.myinvites.listing

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.model.ShareDetail

data class MyInviteShareListingState(
    val isLoading: Boolean = true,
    val shares: List<ShareDetail> = emptyList()
) : BaseViewModel.BaseState