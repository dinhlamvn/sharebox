package com.dinhlam.sharebox.tracking

import com.dinhlam.sharebox.tracking.Event

interface Tracker {
    fun getDefaultParams(): Map<String, Any>
    fun logEvent(event: Event)
}