package com.dinhlam.sharebox.ui.list

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.repository.FolderRepository
import com.dinhlam.sharebox.repository.ShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareListViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val shareRepository: ShareRepository
) : BaseViewModel<ShareListState>(ShareListState()) {

    fun setFolderId(folderId: String) = executeJob {
        val folder = folderRepository.get(folderId) ?: return@executeJob
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
}
