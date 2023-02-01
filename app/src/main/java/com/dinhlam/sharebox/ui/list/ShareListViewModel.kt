package com.dinhlam.sharebox.ui.list

import android.app.Activity
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.repository.FolderRepository
import com.dinhlam.sharebox.repository.ShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareListViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val shareRepository: ShareRepository
) : BaseViewModel<ShareListState>(ShareListState()) {

    fun setFolderId(folderId: String) = backgroundTask {
        val folder = folderRepository.find(folderId) ?: return@backgroundTask
        setState { copy(folderId = folder.id, folderName = folder.name) }
        loadShareList()
    }

    fun setSearchQuery(searchQuery: String) {
        setState { copy(searchQuery = searchQuery) }
        loadShareList()
    }

    fun loadShareList() = execute { state ->
        if (state.searchQuery.isNotEmpty()) {
            val list = shareRepository.search(state.searchQuery)
            setState { copy(shareList = list, isRefreshing = false) }
        } else {
            val folderId = state.folderId ?: return@execute setState { copy(isRefreshing = false) }
            val list = shareRepository.getByFolder(folderId)
            setState { copy(shareList = list, isRefreshing = false) }
        }
    }

    fun deleteShare(shareData: Share) = backgroundTask {
        setState { copy(isRefreshing = true) }
        backgroundTask(onError = {
            setState { copy(isRefreshing = false, toastRes = R.string.delete_share_error) }
        }) {
            val deleted = shareRepository.delete(shareData)
            if (deleted) {
                setState {
                    copy(
                        isRefreshing = false,
                        toastRes = R.string.share_deleted,
                        resultCode = Activity.RESULT_OK
                    )
                }
                loadShareList()
            } else {
                setState { copy(isRefreshing = false, toastRes = R.string.delete_share_error) }
            }
        }
    }

    fun clearToast() = getState { state ->
        if (state.toastRes != 0) {
            setState { copy(toastRes = 0) }
        }
    }
}
