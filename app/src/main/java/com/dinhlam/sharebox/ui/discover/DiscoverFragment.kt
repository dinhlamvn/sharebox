package com.dinhlam.sharebox.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseFragment
import com.dinhlam.sharebox.databinding.FragmentDiscoverBinding
import javax.inject.Inject

class DiscoverFragment @Inject constructor() : BaseFragment<FragmentDiscoverBinding>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDiscoverBinding {
        return FragmentDiscoverBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}