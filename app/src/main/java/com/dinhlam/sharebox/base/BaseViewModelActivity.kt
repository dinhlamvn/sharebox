package com.dinhlam.sharebox.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class BaseViewModelActivity<S : BaseViewModel.BaseState, VM : BaseViewModel<S>, VB : ViewBinding> :
    BaseActivity<VB>(), ViewModelBaseView<S, VM> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onChange(::onStateChanged)
    }
}
