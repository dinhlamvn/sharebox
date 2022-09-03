package com.dinhlam.keepmyshare.ui.dashboard

import androidx.annotation.StringRes
import com.dinhlam.keepmyshare.base.BaseViewModel

class DashboardViewModel : BaseViewModel<DashboardData>(DashboardData()) {

    fun setTitle(title: String) = setData { copy(title = title) }

    fun showToast(@StringRes toastRes: Int) = setData { copy(toastStrRes = toastRes) }
}