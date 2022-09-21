package com.dinhlam.sharesaver.ui.home

import com.dinhlam.sharesaver.base.BaseViewModel
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

    private fun loadFolders() = execute {
        val folders = folderRepository.getAll()
        setData { copy(folders = folders, isRefreshing = false) }
    }


    private fun loadShareData() = executeWithData { data ->
        if (data.selectedFolder == null) {
            return@executeWithData setData { copy(isRefreshing = false) }
        }
        val list = shareRepository.getByFolder(data.selectedFolder.id)
        setData { copy(shareList = list, isRefreshing = false) }
    }

    fun reload() = runWithData { data ->
        if (data.selectedFolder == null) {
            loadFolders()
        } else {
            loadShareData()
        }
    }

    fun onFolderClick(position: Int) = runWithData { data ->
        val folder = data.folders.getOrNull(position) ?: return@runWithData
        setData { copy(selectedFolder = folder, isRefreshing = true) }
        loadShareData()
    }

    fun handleBackPressed(): Boolean {
        return withData { data ->
            if (data.selectedFolder == null) {
                return@withData false
            }
            setData { copy(selectedFolder = null, shareList = emptyList()) }
            true
        }
    }
}