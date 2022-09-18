package com.dinhlam.sharesaver.ui.notifications

import com.dinhlam.sharesaver.base.BaseViewModel

data class NotificationData(
    val title: String = "Notifications",
    val toastStrRes: Int = 0
) : BaseViewModel.BaseData
