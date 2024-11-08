package com.dinhlam.sharebox.tracking.trackers

import android.content.Context
import androidx.core.os.bundleOf
import com.dinhlam.sharebox.tracking.Event
import com.dinhlam.sharebox.tracking.Tracker
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalysisTracker(
    context: Context,
    private val userId: String
) : Tracker {

    private val firebaseAnalysis = FirebaseAnalytics.getInstance(context).apply {
        setDefaultEventParameters(bundleOf(*getDefaultParams().toList().toTypedArray()))
    }

    override fun getDefaultParams(): Map<String, Any> {
        return mapOf("user_id" to userId)
    }

    override fun logEvent(event: Event) {
        firebaseAnalysis.logEvent(
            event.eventName,
            bundleOf(*event.getEventParams().toList().toTypedArray())
        )
    }
}