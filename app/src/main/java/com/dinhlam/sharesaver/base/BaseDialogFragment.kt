package com.dinhlam.sharesaver.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharesaver.extensions.dp
import com.dinhlam.sharesaver.extensions.screenWidth

abstract class BaseDialogFragment<T : BaseViewModel.BaseData, VM : BaseViewModel<T>, VB : ViewBinding> :
    DialogFragment() {

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

    open fun getSpacing(): Int {
        return 0
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { wd ->
            val dialogWidth = screenWidth() - getSpacing().dp(requireContext())
            val dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT
            wd.setLayout(dialogWidth, dialogHeight)
            wd.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}