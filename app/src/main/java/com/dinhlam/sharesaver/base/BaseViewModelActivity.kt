package com.dinhlam.sharesaver.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class BaseViewModelActivity<T : BaseViewModel.BaseData, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseActivity<VB>() {

    abstract val viewModel: VM

    abstract fun onDataChanged(data: T)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.data.observe(this, ::onDataChanged)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onClearConsumers()
    }
}