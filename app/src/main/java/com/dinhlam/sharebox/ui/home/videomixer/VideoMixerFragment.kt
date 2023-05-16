package com.dinhlam.sharebox.ui.home.videomixer

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseFragment
import com.dinhlam.sharebox.databinding.FragmentVideoMixerBinding

class VideoMixerFragment : BaseFragment<FragmentVideoMixerBinding>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentVideoMixerBinding {
        return FragmentVideoMixerBinding.inflate(inflater, container, false)
    }
}