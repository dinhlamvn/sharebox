package com.dinhlam.sharebox.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.reflect.KClass

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding> : BottomSheetDialogFragment() {

    fun interface OnDialogDismissListener {
        fun onDismiss()
    }

    companion object {
        fun <T : BaseBottomSheetDialogFragment<*>> showDialog(
            clazz: KClass<T>,
            fragmentManager: FragmentManager,
            tag: String = "DialogFragment",
            block: T.() -> Unit = { }
        ) {
            val constructor = clazz.java.getConstructor()
            val dialogFragment = constructor.newInstance()
            dialogFragment.apply(block)
            dialogFragment.show(fragmentManager, tag)
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss()
    }

    override fun getTheme(): Int {
        return R.style.Theme_AppBottomSheet
    }
}
