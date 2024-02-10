package com.dinhlam.sharebox.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.extensions.showToast
import kotlinx.coroutines.launch

abstract class BaseViewModelDialogFragment<T : BaseViewModel.BaseState, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseDialogFragment<VB>() {

    abstract val viewModel: VM

    abstract fun onStateChanged(state: T)

    fun <R> getState(viewModel: VM, block: (T) -> R) = block.invoke(viewModel.currentState)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stateFlow.collect(::onStateChanged)
        }
        viewModel.toastEvent.observe(this) { strRes ->
            showToast(strRes)
        }
    }
}
