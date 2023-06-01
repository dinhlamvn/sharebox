package com.dinhlam.sharebox.utils

import java.util.UUID

object CommentUtils {

    fun createCommentId(): String = UUID.randomUUID().toString()
}