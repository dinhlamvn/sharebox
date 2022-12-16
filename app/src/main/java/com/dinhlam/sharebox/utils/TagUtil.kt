package com.dinhlam.sharebox.utils

import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.model.Tag

object TagUtil {

    val tags = listOf(
        Tag.Red,
        Tag.Green,
        Tag.Blue,
        Tag.Yellow,
        Tag.Gray
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
