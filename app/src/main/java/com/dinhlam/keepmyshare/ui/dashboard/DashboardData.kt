package com.dinhlam.keepmyshare.ui.dashboard

import androidx.annotation.StringRes
import com.dinhlam.keepmyshare.base.BaseViewModel

data class DashboardData(
    val title: String = "Dashboard",
    @StringRes val toastStrRes: Int = 0
) : BaseViewModel.BaseData
