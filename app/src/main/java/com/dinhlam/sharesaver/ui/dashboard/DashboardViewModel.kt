package com.dinhlam.sharesaver.ui.dashboard

import androidx.annotation.StringRes
import com.dinhlam.sharesaver.base.BaseViewModel

class DashboardViewModel : BaseViewModel<DashboardData>(DashboardData()) {

    fun setTitle(title: String) = setData { copy(title = title) }

    fun showToast(@StringRes toastRes: Int) = setData { copy(toastStrRes = toastRes) }
}