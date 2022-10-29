package com.dinhlam.sharesaver.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class BaseViewModelActivity<T : BaseViewModel.BaseState, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseActivity<VB>() {

    abstract val viewModel: VM

    abstract fun onDataChanged(data: T)

    fun <R> withState(viewModel: VM, block: (T) -> R) = block.invoke(viewModel.state.value!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.state.observe(this, ::onDataChanged)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onClearConsumers()
    }
}