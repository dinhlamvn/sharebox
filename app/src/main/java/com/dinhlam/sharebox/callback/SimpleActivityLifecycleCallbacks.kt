package com.dinhlam.sharebox.callback

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

open class SimpleActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        /* Do nothing */
    }

    override fun onActivityStarted(activity: Activity) {
        /* Do nothing */
    }

    override fun onActivityResumed(activity: Activity) {
        /* Do nothing */
    }

    override fun onActivityPaused(activity: Activity) {
        /* Do nothing */
    }

    override fun onActivityStopped(activity: Activity) {
        /* Do nothing */
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        /* Do nothing */
    }

    override fun onActivityDestroyed(activity: Activity) {
        /* Do nothing */
    }
}