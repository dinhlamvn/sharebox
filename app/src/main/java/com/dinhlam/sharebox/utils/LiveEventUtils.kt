package com.dinhlam.sharebox.utils

import com.dinhlam.sharebox.base.OneTimeLiveData

object LiveEventUtils {
    val createBoxEvent = OneTimeLiveData<String?>(null)
    val retryFirstLoadEvent = OneTimeLiveData<Unit>(null)
}