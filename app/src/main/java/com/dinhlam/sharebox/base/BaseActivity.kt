package com.dinhlam.sharebox.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected val activityScope by lazyOf(MainScope() + CoroutineName("ActivityScope"))

    abstract fun onCreateViewBinding(): VB

    private var binding: VB? = null

    protected val viewBinding: VB
        get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = onCreateViewBinding()
        setContentView(binding!!.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
