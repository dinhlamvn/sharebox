package com.dinhlam.sharesaver.ui.notifications

import androidx.annotation.StringRes
import com.dinhlam.sharesaver.base.BaseViewModel

class NotificationsViewModel : BaseViewModel<NotificationData>(NotificationData()) {
    fun showToast(@StringRes toastRes: Int) = setData { copy(toastStrRes = toastRes) }

    fun setTitle(title: String) = setData { copy(title = title) }
}