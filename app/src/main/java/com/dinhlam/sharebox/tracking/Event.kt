package com.dinhlam.sharebox.tracking

interface Event {
    val eventName: String
    fun getEventParams(): Map<String, Any>
}