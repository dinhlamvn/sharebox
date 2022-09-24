package com.dinhlam.sharesaver.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class BaseViewModelActivity<T : BaseViewModel.BaseData, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseActivity<VB>() {

    abstract val viewModel: VM

    abstract fun onDataChanged(data: T)

    fun <R> withData(viewModel: VM, block: (T) -> R) = block.invoke(viewModel.data.value!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.data.observe(this, ::onDataChanged)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onClearConsumers()
    }
}