package com.dinhlam.sharebox.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseFragment
import com.dinhlam.sharebox.databinding.FragmentDownloadBinding
import javax.inject.Inject

class DownloadFragment @Inject constructor() : BaseFragment<FragmentDownloadBinding>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDownloadBinding {
        return FragmentDownloadBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}