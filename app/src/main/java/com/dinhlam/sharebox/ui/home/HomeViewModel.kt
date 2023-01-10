package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.repository.FolderRepository
import com.dinhlam.sharebox.repository.ShareRepository
import com.dinhlam.sharebox.utils.TagUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val folderRepository: FolderRepository, private val shareRepository: ShareRepository
) : BaseViewModel<HomeState>(HomeState()) {

    init {
        loadFolders()
        loadShareList()
    }

    fun loadFolders() = execute { state ->
        val folders = if (state.tag == null) {
            folderRepository.getAll(state.sortType)
        } else {
            folderRepository.getByTag(state.tag)
        }
        setState { copy(folders = folders, isRefreshing = false) }
    }

    private fun loadShareList() = executeJob {
        val shares = shareRepository.getRecentList()
        setState { copy(shareList = shares) }
    }

    fun onFolderClick(position: Int) = execute { state ->
        val folder = state.folders.getOrNull(position) ?: return@execute
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder, shareCount, HomeState.FolderActionConfirmation.FolderActionType.OPEN
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

    fun clearToast() = getState { state ->
        if (state.toastRes != 0) {
            setState { copy(toastRes = 0) }
        }
    }

    fun processFolderForDelete(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder, shareCount, HomeState.FolderActionConfirmation.FolderActionType.DELETE
                )
            )
        }
    }

    fun processFolderForRename(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder, shareCount, HomeState.FolderActionConfirmation.FolderActionType.RENAME
                )
            )
        }
    }

    fun processFolderForDetail(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder, shareCount, HomeState.FolderActionConfirmation.FolderActionType.DETAIL
                )
            )
        }
    }

    fun processFolderForTag(folder: Folder) = executeJob {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder, shareCount, HomeState.FolderActionConfirmation.FolderActionType.TAG
                )
            )
        }
    }

    fun clearFolderActionConfirmation() = setState {
        copy(folderActionConfirmation = null)
    }

    fun deleteFolderAfterPasswordVerified() = getState { state ->
        val folder = state.folderActionConfirmation?.folder
            ?: return@getState clearFolderActionConfirmation()
        deleteFolder(folder)
    }

    fun renameFolderAfterPasswordVerified() = getState { state ->
        val confirmation =
            state.folderActionConfirmation ?: return@getState clearFolderActionConfirmation()
        val newConfirmation = confirmation.copy(ignorePassword = true)
        setState { copy(folderActionConfirmation = newConfirmation) }
    }

    fun showDetailFolderAfterPasswordVerified() = getState { state ->
        val confirmation =
            state.folderActionConfirmation ?: return@getState clearFolderActionConfirmation()
        val newConfirmation = confirmation.copy(ignorePassword = true)
        setState { copy(folderActionConfirmation = newConfirmation) }
    }

    fun openFolderAfterPasswordVerified(isRemindPassword: Boolean) = getState { state ->
        val folder = state.folderActionConfirmation?.folder
            ?: return@getState clearFolderActionConfirmation()
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
                R.id.tag_red, R.id.tag_green, R.id.tag_blue, R.id.tag_yellow, R.id.tag_gray
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

    fun setSortType(sortType: SortType) {
        setState {
            copy(sortType = sortType)
        }
        getState {
            loadFolders()
        }
    }
}
