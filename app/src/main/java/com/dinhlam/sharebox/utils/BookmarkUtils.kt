package com.dinhlam.sharebox.utils

import java.util.UUID

object BookmarkUtils {

    fun createBookmarkCollectionId() = UUID.randomUUID().toString()
}