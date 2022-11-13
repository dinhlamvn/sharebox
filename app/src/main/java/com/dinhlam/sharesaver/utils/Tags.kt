package com.dinhlam.sharesaver.utils

import android.graphics.Color
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.model.Tag

object Tags {

    val tags = listOf(
        Tag(1, "Red", Color.RED),
        Tag(2, "Green", Color.GREEN),
        Tag(3, "Blue", Color.BLUE),
        Tag(4, "Yellow", Color.YELLOW),
        Tag(5, "Gray", Color.GRAY)
    )

    fun setFolderTag(id: Int, folder: Folder): Folder {
        if (!tags.any { tag -> tag.id == id }) {
            error("Tag id incorrect")
        }
        return folder.copy(tag = id)
    }

    fun getTag(tag: Int?): Tag? {
        val tagId = tag ?: return null
        return tags.firstOrNull { it.id == tagId }
    }
}