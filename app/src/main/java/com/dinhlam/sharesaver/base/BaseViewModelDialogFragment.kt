package com.dinhlam.sharesaver.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding

abstract class BaseViewModelDialogFragment<T : BaseViewModel.BaseState, VM : BaseViewModel<T>, VB : ViewBinding> :
    BaseDialogFragment<VB>() {

    abstract val viewModel: VM

    abstract fun onDataChanged(data: T)

    fun <R> withData(viewModel: VM, block: (T) -> R) = block.invoke(viewModel.state.value!!)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.state.observe(viewLifecycleOwner, ::onDataChanged)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onClearConsumers()
    }
}