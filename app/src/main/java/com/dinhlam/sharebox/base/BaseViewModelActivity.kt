package com.dinhlam.sharebox.base

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

abstract class BaseViewModelActivity<T : BaseViewModel.BaseState, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseActivity<VB>() {

    abstract val viewModel: VM

    abstract fun onStateChanged(state: T)

    fun <R> getState(viewModel: VM, block: (T) -> R) = block.invoke(viewModel.currentState)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collect(::onStateChanged)
            }
        }
    }
}
