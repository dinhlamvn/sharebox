package com.dinhlam.sharesaver.utils

import com.dinhlam.sharesaver.model.Folder

object FolderUtils {

    private val textFolder = Folder("folder_text", "Texts", "text")
    private val webLinkFolder = Folder("folder_web_link", "Web-Links", "web-link")
    private val imageFolder = Folder("folder_image", "Images", "image")
    private val customFolders: List<Folder> = emptyList()

    fun getFolderByShareType(shareType: String): Folder {
        val folders = getFolders()
        return folders.firstOrNull { folder -> folder.shareType == shareType }
            ?: throw IllegalStateException("No such folder fot share type: $shareType")
    }

    fun getFolders(): List<Folder> =
        listOf(textFolder, webLinkFolder, imageFolder).plus(customFolders)
}