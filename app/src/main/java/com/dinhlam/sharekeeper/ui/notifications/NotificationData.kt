package com.dinhlam.sharekeeper.ui.notifications

import com.dinhlam.sharekeeper.base.BaseViewModel

data class NotificationData(
    val title: String = "Notifications",
    val toastStrRes: Int = 0
) : BaseViewModel.BaseData
