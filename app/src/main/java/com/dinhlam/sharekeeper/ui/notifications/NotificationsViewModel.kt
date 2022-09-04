package com.dinhlam.sharekeeper.ui.notifications

import androidx.annotation.StringRes
import com.dinhlam.sharekeeper.base.BaseViewModel

class NotificationsViewModel : BaseViewModel<NotificationData>(NotificationData()) {
    fun showToast(@StringRes toastRes: Int) = setData { copy(toastStrRes = toastRes) }

    fun setTitle(title: String) = setData { copy(title = title) }
}