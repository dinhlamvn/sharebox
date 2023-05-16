package com.dinhlam.sharebox.utils

import java.util.UUID

object BookmarkUtils {

    const val THUMBNAIL_FAKE = "https://images.pexels.com/photos/2754200/pexels-photo-2754200.jpeg"

    fun createBookmarkCollectionId() = UUID.randomUUID().toString()
}