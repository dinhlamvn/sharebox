package com.dinhlam.sharesaver.ui.dialog.folder.detail

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.repository.FolderRepository
import com.dinhlam.sharesaver.repository.ShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FolderDetailDialogViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val shareRepository: ShareRepository
) : BaseViewModel<FolderDetailDialogData>(FolderDetailDialogData()) {

    fun loadFolderData(folderId: String) = executeJob {
        val folder = folderRepository.get(folderId)
        val shareCount = shareRepository.countByFolder(folderId)
        setData { copy(folder = folder, shareCount = shareCount) }
    }
}