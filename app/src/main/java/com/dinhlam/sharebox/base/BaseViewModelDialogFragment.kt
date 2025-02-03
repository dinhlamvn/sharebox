package com.dinhlam.sharebox.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding

abstract class BaseViewModelDialogFragment<T : BaseViewModel.BaseState, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseDialogFragment<VB>(), ViewModelBaseView<T, VM> {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onChange(::onStateChanged)
    }
}
