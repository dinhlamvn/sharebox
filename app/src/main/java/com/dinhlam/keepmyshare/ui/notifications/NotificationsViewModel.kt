package com.dinhlam.keepmyshare.ui.notifications

import androidx.annotation.StringRes
import com.dinhlam.keepmyshare.base.BaseViewModel

class NotificationsViewModel : BaseViewModel<NotificationData>(NotificationData()) {
    fun showToast(@StringRes toastRes: Int) = setData { copy(toastStrRes = toastRes) }

    fun setTitle(title: String) = setData { copy(title = title) }
}