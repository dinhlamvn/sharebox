package com.dinhlam.sharebox.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding

abstract class BaseViewModelFragment<S : BaseViewModel.BaseState, VM : BaseViewModel<S>, VB : ViewBinding> :
    BaseFragment<VB>(), ViewModelBaseView<S, VM> {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onChange(::onStateChanged)
    }
}
