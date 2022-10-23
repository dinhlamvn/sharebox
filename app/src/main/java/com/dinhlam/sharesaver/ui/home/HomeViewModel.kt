package com.dinhlam.sharesaver.ui.home

import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.repository.FolderRepository
import com.dinhlam.sharesaver.repository.ShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val shareRepository: ShareRepository
) : BaseViewModel<HomeData>(HomeData()) {

    init {
        loadFolders()
    }

    fun loadFolders() = executeJob {
        val folders = folderRepository.getAll()
        setData { copy(folders = folders, isRefreshing = false) }
    }

    fun onFolderClick(position: Int) = execute { data ->
        val folder = data.folders.getOrNull(position) ?: return@execute
        val shareCount = shareRepository.countByFolder(folder.id)
        setData {
            copy(
                folderActionConfirmation = HomeData.FolderActionConfirmation(
                    folder, shareCount, HomeData.FolderActionConfirmation.FolderActionType.OPEN
                )
            )
        }
    }

    private fun openFolder(folder: Folder) {
        setData { copy(folderActionConfirmation = null, folderToOpen = folder) }
    }

    fun deleteFolder(folder: Folder) {
        setData { copy(showProgress = true, folderActionConfirmation = null) }
        executeJob(onError = {
            setData { copy(showProgress = false, toastRes = R.string.delete_folder_error) }
        }) {
            val deleted = folderRepository.delete(folder)
            if (deleted) {
                setData { copy(showProgress = false, toastRes = R.string.delete_folder_success) }
                loadFolders()
            } else {
                setData { copy(showProgress = false, toastRes = R.string.delete_folder_error) }
            }
        }
    }

    fun clearToast() = runWithData { data ->
        if (data.toastRes != 0) {
            setData { copy(toastRes = 0) }
        }
    }

    fun processFolderForDelete(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setData {
            copy(
                folderActionConfirmation = HomeData.FolderActionConfirmation(
                    folder, shareCount, HomeData.FolderActionConfirmation.FolderActionType.DELETE
                )
            )
        }
    }

    fun processFolderForRename(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setData {
            copy(
                folderActionConfirmation = HomeData.FolderActionConfirmation(
                    folder, shareCount, HomeData.FolderActionConfirmation.FolderActionType.RENAME
                )
            )
        }
    }

    fun processFolderForDetail(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setData {
            copy(
                folderActionConfirmation = HomeData.FolderActionConfirmation(
                    folder, shareCount, HomeData.FolderActionConfirmation.FolderActionType.DETAIL
                )
            )
        }
    }

    fun clearFolderActionConfirmation() = setData {
        copy(folderActionConfirmation = null)
    }

    fun deleteFolderAfterPasswordVerified() = runWithData { data ->
        val folder = data.folderActionConfirmation?.folder
            ?: return@runWithData clearFolderActionConfirmation()
        deleteFolder(folder)
    }

    fun renameFolderAfterPasswordVerified() = withData { data ->
        val confirmation =
            data.folderActionConfirmation ?: return@withData clearFolderActionConfirmation()
        val newConfirmation = confirmation.copy(ignorePassword = true)
        setData { copy(folderActionConfirmation = newConfirmation) }
    }

    fun showDetailFolderAfterPasswordVerified() = withData { data ->
        val confirmation =
            data.folderActionConfirmation ?: return@withData clearFolderActionConfirmation()
        val newConfirmation = confirmation.copy(ignorePassword = true)
        setData { copy(folderActionConfirmation = newConfirmation) }
    }

    fun openFolderAfterPasswordVerified(isRemindPassword: Boolean) = runWithData { data ->
        val folder = data.folderActionConfirmation?.folder
            ?: return@runWithData clearFolderActionConfirmation()
        if (isRemindPassword) {
            setData { copy(folderPasswordConfirmRemind = folderPasswordConfirmRemind.plus(folder.id)) }
        }
        openFolder(folder)
    }

    fun clearOpenFolder() = setData {
        copy(folderToOpen = null)
    }
}