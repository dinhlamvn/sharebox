package com.dinhlam.sharebox.tracking

object TrackerManager {

    private val trackers = mutableSetOf<Tracker>()

    fun addTracker(tracker: Tracker) {
        trackers.add(tracker)
    }

    fun removeTracker(tracker: Tracker) {
        trackers.remove(tracker)
    }

    fun logEvent(event: Event) {
        trackers.forEach { tracker ->
            tracker.logEvent(event)
        }
    }
}