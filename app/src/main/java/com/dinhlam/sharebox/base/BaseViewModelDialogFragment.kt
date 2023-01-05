package com.dinhlam.sharebox.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

abstract class BaseViewModelDialogFragment<T : BaseViewModel.BaseState, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseDialogFragment<VB>() {

    abstract val viewModel: VM

    abstract fun onStateChanged(state: T)

    fun <R> withState(viewModel: VM, block: (T) -> R) = block.invoke(viewModel.state.value)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect(::onStateChanged)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onClearConsumers()
    }
}
