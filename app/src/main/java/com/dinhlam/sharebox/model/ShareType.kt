package com.dinhlam.sharebox.model

enum class ShareType(val type: String) {
    UNKNOWN("unknown"),
    WEB("web-link"),
    IMAGE("image"),
    TEXT("text"),
    MULTIPLE_IMAGE("multiple-image"),
}