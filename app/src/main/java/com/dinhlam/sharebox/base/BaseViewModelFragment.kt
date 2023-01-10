package com.dinhlam.sharebox.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

abstract class BaseViewModelFragment<T : BaseViewModel.BaseState, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseFragment<VB>() {

    abstract val viewModel: VM

    abstract fun onStateChanged(state: T)

    fun <R> getState(viewModel: VM, block: (T) -> R) = block.invoke(viewModel.state.value)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::onStateChanged)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onClearConsumers()
    }
}
