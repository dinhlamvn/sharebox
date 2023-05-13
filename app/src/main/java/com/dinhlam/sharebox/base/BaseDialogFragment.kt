package com.dinhlam.sharebox.base

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenWidth

abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {

    fun interface OnDialogDismissListener {
        fun onDismiss()
    }

    companion object {
        fun <T : BaseDialogFragment<*>> showDialog(
            dialog: T,
            fragmentManager: FragmentManager,
            tag: String = "DialogFragment",
            block: T.() -> Unit = { }
        ) {
            dialog.apply(block)
            dialog.show(fragmentManager, tag)
        }
    }

    abstract fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    private var binding: VB? = null

    protected val viewBinding: VB
        get() = binding!!

    var dismissListener: OnDialogDismissListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        onViewPreLoad(savedInstanceState)
        binding = onCreateViewBinding(inflater, container)
        return binding!!.root
    }

    open fun onViewPreLoad(savedInstanceState: Bundle?) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewDidLoad(view, savedInstanceState)
    }

    abstract fun onViewDidLoad(view: View, savedInstanceState: Bundle?)

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss()
    }

    open fun getSpacing(): Int {
        return 0
    }
}
