package com.dinhlam.sharesaver.ui.home

import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.repository.FolderRepository
import com.dinhlam.sharesaver.repository.ShareRepository
import com.dinhlam.sharesaver.utils.TagUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val shareRepository: ShareRepository
) : BaseViewModel<HomeState>(HomeState()) {

    init {
        loadFolders()
    }

    fun loadFolders() = execute { state ->
        val folders = if (state.tag == null) {
            folderRepository.getAll()
        } else {
            folderRepository.getByTag(state.tag)
        }
        setState { copy(folders = folders, isRefreshing = false) }
    }

    fun onFolderClick(position: Int) = execute { data ->
        val folder = data.folders.getOrNull(position) ?: return@execute
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder,
                    shareCount,
                    HomeState.FolderActionConfirmation.FolderActionType.OPEN
                )
            )
        }
    }

    private fun openFolder(folder: Folder) {
        setState { copy(folderActionConfirmation = null, folderToOpen = folder) }
    }

    fun deleteFolder(folder: Folder) {
        setState { copy(showProgress = true, folderActionConfirmation = null) }
        executeJob(onError = {
            setState { copy(showProgress = false, toastRes = R.string.delete_folder_error) }
        }) {
            val deleted = folderRepository.delete(folder)
            if (deleted) {
                setState { copy(showProgress = false, toastRes = R.string.delete_folder_success) }
                loadFolders()
            } else {
                setState { copy(showProgress = false, toastRes = R.string.delete_folder_error) }
            }
        }
    }

    fun clearToast() = withState { data ->
        if (data.toastRes != 0) {
            setState { copy(toastRes = 0) }
        }
    }

    fun processFolderForDelete(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder,
                    shareCount,
                    HomeState.FolderActionConfirmation.FolderActionType.DELETE
                )
            )
        }
    }

    fun processFolderForRename(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder,
                    shareCount,
                    HomeState.FolderActionConfirmation.FolderActionType.RENAME
                )
            )
        }
    }

    fun processFolderForDetail(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder,
                    shareCount,
                    HomeState.FolderActionConfirmation.FolderActionType.DETAIL
                )
            )
        }
    }

    fun processFolderForTag(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder,
                    shareCount,
                    HomeState.FolderActionConfirmation.FolderActionType.TAG
                )
            )
        }
    }

    fun clearFolderActionConfirmation() = setState {
        copy(folderActionConfirmation = null)
    }

    fun deleteFolderAfterPasswordVerified() = withState { data ->
        val folder = data.folderActionConfirmation?.folder
            ?: return@withState clearFolderActionConfirmation()
        deleteFolder(folder)
    }

    fun renameFolderAfterPasswordVerified() = withState { data ->
        val confirmation =
            data.folderActionConfirmation ?: return@withState clearFolderActionConfirmation()
        val newConfirmation = confirmation.copy(ignorePassword = true)
        setState { copy(folderActionConfirmation = newConfirmation) }
    }

    fun showDetailFolderAfterPasswordVerified() = withState { data ->
        val confirmation =
            data.folderActionConfirmation ?: return@withState clearFolderActionConfirmation()
        val newConfirmation = confirmation.copy(ignorePassword = true)
        setState { copy(folderActionConfirmation = newConfirmation) }
    }

    fun openFolderAfterPasswordVerified(isRemindPassword: Boolean) = withState { data ->
        val folder = data.folderActionConfirmation?.folder
            ?: return@withState clearFolderActionConfirmation()
        if (isRemindPassword) {
            setState { copy(folderPasswordConfirmRemind = folderPasswordConfirmRemind.plus(folder.id)) }
        }
        openFolder(folder)
    }

    fun clearOpenFolder() = setState {
        copy(folderToOpen = null)
    }

    fun setFolderTag(tagId: Int) = execute { state ->
        if (tagId != 0) {
            val folder = state.folderActionConfirmation?.folder ?: return@execute setState {
                copy(folderActionConfirmation = null)
            }
            folderRepository.update(TagUtil.setFolderTag(tagId, folder))
            loadFolders()
            clearFolderActionConfirmation()
        } else {
            clearFolderActionConfirmation()
        }
    }

    fun isHandleByTagSelected(itemId: Int): Boolean {
        if (itemId !in listOf(
                R.id.tag_red,
                R.id.tag_green,
                R.id.tag_blue,
                R.id.tag_yellow,
                R.id.tag_gray
            )
        ) {
            return false
        }
        loadFolderByTag(itemId)
        return true
    }

    private fun loadFolderByTag(tagId: Int) = executeJob {
        val folders = folderRepository.getByTag(tagId)
        setState { copy(folders = folders, tag = tagId) }
    }

    fun clearSelectedTag() {
        setState { copy(tag = null) }
        loadFolders()
    }

    fun removeTag(folder: Folder) = execute {
        val newFolder = folder.copy(tag = null)
        folderRepository.update(newFolder)
        loadFolders()
    }
}
