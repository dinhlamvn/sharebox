package com.dinhlam.sharebox.base

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.di.DefaultFragmentFactoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
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
        get() = checkNotNull(_binding) { "ViewBinding accessed after onDestroy()" }

    override fun onCreate(savedInstanceState: Bundle?) {
        val entryPoint = EntryPointAccessors.fromActivity(
            this,
            DefaultFragmentFactoryEntryPoint::class.java
        )
        supportFragmentManager.fragmentFactory = entryPoint.getFragmentFactory()

        super.onCreate(savedInstanceState)
        _binding = onCreateViewBinding()
        setContentView(_binding!!.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        activityScope.cancel()
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        if (supportNavBack()) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    open fun supportNavBack(): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (supportNavBack() && item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
