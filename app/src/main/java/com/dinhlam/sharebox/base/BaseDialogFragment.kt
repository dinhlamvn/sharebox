package com.dinhlam.sharebox.base

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenWidth
import com.google.android.material.dialog.MaterialAlertDialogBuilder

abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {

    fun interface OnDialogDismissListener {
        fun onDismiss()
    }

    abstract fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    private var binding: VB? = null

    protected val viewBinding: VB
        get() = binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            binding = onCreateViewBinding(layoutInflater, null)
            setView(viewBinding.root)
        }.create()
    }

    override fun getView(): View? {
        return viewBinding.root
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { wd ->
            val dialogWidth = screenWidth() - getSpacing().dp()
            val dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT
            wd.setLayout(dialogWidth, dialogHeight)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.cast<OnDialogDismissListener>()?.onDismiss()
            ?: parentFragment.cast<OnDialogDismissListener>()?.onDismiss()
    }

    open fun getSpacing(): Int {
        return 0
    }
}
