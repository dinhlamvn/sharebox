package com.dinhlam.keepmyshare.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.dinhlam.keepmyshare.R
import com.dinhlam.keepmyshare.base.BaseFragment
import com.dinhlam.keepmyshare.databinding.FragmentNotificationsBinding
import com.dinhlam.keepmyshare.ui.dashboard.DashboardData

class NotificationsFragment :
    BaseFragment<NotificationData, NotificationsViewModel, FragmentNotificationsBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNotificationsBinding {
        return FragmentNotificationsBinding.inflate(inflater, container, false)
    }

    override val viewModel: NotificationsViewModel by viewModels()

    override fun onDataChanged(data: NotificationData) {
        viewBinding.textNotifications.text = data.title
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewModel.setTitle("Notifications")

        viewBinding.textNotifications.setOnClickListener {
            viewModel.showToast(R.string.title_notifications)
        }

        viewModel.listen(DashboardData::toastStrRes) { value, isChanged ->
            if (value != 0) {
                Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show()
            }
        }
    }
}