package com.dinhlam.sharebox.ui.home

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.repository.FolderRepository
import com.dinhlam.sharebox.repository.ShareRepository
import com.dinhlam.sharebox.utils.TagUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val shareRepository: ShareRepository,
    private val appSharePref: AppSharePref
) : BaseViewModel<HomeState>(HomeState(isShowRecentlyShare = appSharePref.isShowRecentlyShare())) {

    init {
        loadShareListRecently()
    }

    fun loadFolders() = execute { state ->
        setState { copy(isRefreshing = true) }

        val folders = state.tag?.let { tag ->
            folderRepository.getByTag(tag)
        } ?: folderRepository.getAll(state.sortType)

        setState { copy(folders = folders, isRefreshing = false) }
        loadFolderShareCount()
    }

    fun loadFolderShareCount() = backgroundTask {
        val list = shareRepository.getFolderShareCount()
        setState { copy(folderShareCountMap = list.associate { it.id to it.shareCount }) }
    }

    fun reloadShareRecently() = getState { state ->
        if (state.isShowRecentlyShare != appSharePref.isShowRecentlyShare()) {
            setState { copy(isShowRecentlyShare = appSharePref.isShowRecentlyShare()) }
            loadShareListRecently()
        }
    }

    fun loadShareListRecently() = execute { state ->
        if (state.isShowRecentlyShare) {
            val shares = shareRepository.getRecentList()
            setState { copy(shareList = shares) }
        } else {
            setState { copy(shareList = emptyList()) }
        }
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
        backgroundTask(onError = {
            setState { copy(showProgress = false, toastRes = R.string.delete_folder_error) }
        }) {
            val deleted = folderRepository.delete(folder)
            if (deleted) {
                setState { copy(showProgress = false, toastRes = R.string.delete_folder_success) }
                loadFolders()
                loadShareListRecently()
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

    fun processFolderForDelete(folder: Folder) = backgroundTask {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder, shareCount, HomeState.FolderActionConfirmation.FolderActionType.DELETE
                )
            )
        }
    }

    fun processFolderForRename(folder: Folder) = backgroundTask {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder, shareCount, HomeState.FolderActionConfirmation.FolderActionType.RENAME
                )
            )
        }
    }

    fun processFolderForDetail(folder: Folder) = backgroundTask {
        val shareCount = shareRepository.countByFolder(folder.id)
        setState {
            copy(
                folderActionConfirmation = HomeState.FolderActionConfirmation(
                    folder, shareCount, HomeState.FolderActionConfirmation.FolderActionType.DETAIL
                )
            )
        }
    }

    fun processFolderForTag(folder: Folder) = execute { state ->
        if (state.folderActionConfirmation != null) {
            setState { copy(folderActionConfirmation = null) }
        }
        getState {
            val shareCount = shareRepository.countByFolder(folder.id)
            setState {
                copy(
                    folderActionConfirmation = HomeState.FolderActionConfirmation(
                        folder, shareCount, HomeState.FolderActionConfirmation.FolderActionType.TAG
                    )
                )
            }
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
        val folder = state.folderActionConfirmation?.folder ?: return@execute setState {
            copy(folderActionConfirmation = null)
        }

        if (tagId != -1) {
            folderRepository.update(TagUtil.setFolderTag(tagId, folder))
            loadFolders()
            clearFolderActionConfirmation()
        } else {
            removeTag(folder)
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

    private fun loadFolderByTag(tagId: Int) = backgroundTask {
        val folders = folderRepository.getByTag(tagId)
        setState { copy(folders = folders, tag = tagId) }
    }

    fun clearSelectedTag() {
        setState { copy(tag = null) }
        loadFolders()
    }

    private fun removeTag(folder: Folder) = execute {
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

    fun processFolderForResetPassword(folder: Folder) = setState {
        copy(
            folderActionConfirmation = HomeState.FolderActionConfirmation(
                folder, 0, HomeState.FolderActionConfirmation.FolderActionType.RESET_PASSWORD
            )
        )
    }

    fun deleteShare(shareData: Share) = backgroundTask {
        setState { copy(showProgress = true) }
        backgroundTask(onError = {
            setState { copy(showProgress = false, toastRes = R.string.delete_share_error) }
        }) {
            val deleted = shareRepository.delete(shareData)
            if (deleted) {
                setState { copy(showProgress = false, toastRes = R.string.share_deleted) }
                loadFolders()
                loadShareListRecently()
            } else {
                setState { copy(showProgress = false, toastRes = R.string.delete_share_error) }
            }
        }
    }
}
