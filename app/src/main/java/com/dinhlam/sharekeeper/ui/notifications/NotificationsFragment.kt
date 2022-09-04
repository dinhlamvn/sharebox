package com.dinhlam.sharekeeper.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.dinhlam.sharekeeper.R
import com.dinhlam.sharekeeper.base.BaseFragment
import com.dinhlam.sharekeeper.databinding.FragmentNotificationsBinding
import com.dinhlam.sharekeeper.ui.dashboard.DashboardData

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

        viewModel.consume(DashboardData::toastStrRes) { value ->
            if (value != 0) {
                Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show()
            }
        }
    }
}