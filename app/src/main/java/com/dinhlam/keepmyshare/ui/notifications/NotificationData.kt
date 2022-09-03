package com.dinhlam.keepmyshare.ui.notifications

import com.dinhlam.keepmyshare.base.BaseViewModel

data class NotificationData(
    val title: String = "Notifications",
    val toastStrRes: Int = 0
) : BaseViewModel.BaseData
