package com.dinhlam.sharebox.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.di.DefaultFragmentFactoryEntryPoint
import dagger.hilt.android.EntryPointAccessors

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    abstract fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding!!

    protected open val isOverrideBackPressedCallback: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = onCreateViewBinding(inflater, container)
        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachBackPressedCallback()
        val entryPoint = EntryPointAccessors.fromFragment(
            this,
            DefaultFragmentFactoryEntryPoint::class.java
        )
        childFragmentManager.fragmentFactory = entryPoint.getFragmentFactory()
    }

    private fun attachBackPressedCallback() {
        if (!isOverrideBackPressedCallback) {
            return
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(callback)
    }

    protected open fun onBackPressed() {

    }
}
