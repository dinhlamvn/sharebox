package com.dinhlam.keepmyshare.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.dinhlam.keepmyshare.R
import com.dinhlam.keepmyshare.base.BaseFragment
import com.dinhlam.keepmyshare.databinding.FragmentDashboardBinding

class DashboardFragment :
    BaseFragment<DashboardData, DashboardViewModel, FragmentDashboardBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDashboardBinding {
        return FragmentDashboardBinding.inflate(inflater, container, false)
    }

    override val viewModel: DashboardViewModel by viewModels()

    override fun onDataChanged(data: DashboardData) {
        viewBinding.textDashboard.text = data.title
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewModel.setTitle("Dashboard")

        viewBinding.textDashboard.setOnClickListener {
            viewModel.showToast(R.string.title_dashboard)
        }

        viewModel.listen(DashboardData::toastStrRes) { value, isChanged ->
            if (value != 0) {
                Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show()
            }
        }
    }
}