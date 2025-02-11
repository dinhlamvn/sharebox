package com.dinhlam.sharebox.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LiveEvents {
    private val _discoverTabChangeEvent = MutableLiveData(0)
    val discoverTabChangeEvent: LiveData<Int>
        get() = _discoverTabChangeEvent

    fun changeDiscoverTab(tab: Int) = _discoverTabChangeEvent.postValue(tab)

    private val _onBottomSheetShareActionRefreshEvent = MutableLiveData(Unit)
    val onBottomSheetShareActionRefreshEvent: LiveData<Unit>
        get() = _onBottomSheetShareActionRefreshEvent

    fun requestBottomSheetShareActionRefresh() =
        _onBottomSheetShareActionRefreshEvent.postValue(Unit)
}