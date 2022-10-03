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
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharesaver.extensions.dp
import com.dinhlam.sharesaver.extensions.screenWidth
import kotlin.reflect.KClass

abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {

    companion object {
        fun <T : BaseDialogFragment<*>> showDialog(
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

    open fun getSpacing(): Int {
        return 0
    }
}