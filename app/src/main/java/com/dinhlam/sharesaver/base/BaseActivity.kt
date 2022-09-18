package com.dinhlam.sharesaver.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

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