package com.dinhlam.sharebox.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding

abstract class BaseBottomSheetViewModelDialogFragment<T : BaseViewModel.BaseState, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseBottomSheetDialogFragment<VB>(), ViewModelBaseView<T, VM> {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onChange<T>(::onStateChanged)
    }
}
