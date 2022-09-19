package com.dinhlam.sharesaver.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T : BaseViewModel.BaseData, VM : BaseViewModel<T>, VB : ViewBinding> :
    Fragment() {

    @NonNull
    abstract fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    abstract val viewModel: VM

    private var binding: VB? = null

    protected val viewBinding: VB
        get() = binding!!

    abstract fun onDataChanged(data: T)

    fun withData(viewModel: VM, block: (T) -> Unit) = block.invoke(viewModel.data.value!!)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        onViewPreLoad(savedInstanceState)
        binding = onCreateViewBinding(inflater, container)
        return binding!!.root
    }

    open fun onViewPreLoad(savedInstanceState: Bundle?) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.data.observe(viewLifecycleOwner, ::onDataChanged)
        onViewDidLoad(view, savedInstanceState)
    }

    abstract fun onViewDidLoad(view: View, savedInstanceState: Bundle?)

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        viewModel.onClearConsumers()
    }
}