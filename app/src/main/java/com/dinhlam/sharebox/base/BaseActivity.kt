package com.dinhlam.sharebox.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected val activityScope by lazyOf(MainScope() + CoroutineName("ActivityScope") + Job())

    abstract fun onCreateViewBinding(): VB

    private var _binding: VB? = null

    protected val binding: VB
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = onCreateViewBinding()
        setContentView(_binding!!.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        activityScope.cancel()
    }
}
