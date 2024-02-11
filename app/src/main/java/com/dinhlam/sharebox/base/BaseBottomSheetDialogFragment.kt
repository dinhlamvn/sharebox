package com.dinhlam.sharebox.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.extensions.cast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding> : BottomSheetDialogFragment() {

    fun interface OnBottomSheetDismissListener {
        fun onDismiss()
    }

    abstract fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = onCreateViewBinding(inflater, container)
        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.cast<OnBottomSheetDismissListener>()?.onDismiss()
            ?: parentFragment?.cast<OnBottomSheetDismissListener>()?.onDismiss()
    }

    override fun onStart() {
        dialog?.cast<BottomSheetDialog>()?.behavior?.apply {
            onConfigBottomSheetBehavior(this)
        }
        super.onStart()
    }

    open fun onConfigBottomSheetBehavior(behavior: BottomSheetBehavior<*>) {}
}
