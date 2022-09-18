package com.dinhlam.sharesaver.ui.dashboard

import androidx.annotation.StringRes
import com.dinhlam.sharesaver.base.BaseViewModel

data class DashboardData(
    val title: String = "Dashboard",
    @StringRes val toastStrRes: Int = 0
) : BaseViewModel.BaseData
