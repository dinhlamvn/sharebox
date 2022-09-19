package com.dinhlam.sharesaver.ui.home

import com.dinhlam.sharesaver.base.BaseViewModel
import com.dinhlam.sharesaver.database.AppDatabase
import com.dinhlam.sharesaver.model.Folder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val appDatabase: AppDatabase) :
    BaseViewModel<HomeData>(HomeData()) {

    init {
        loadFolders()
    }

    private fun loadFolders() = execute {
        setData { copy(isRefreshing = true) }
        val folder1 = Folder("folder1", "web-link", "web-link")
        val folder2 = Folder("folder2", "image", "image")
        val folders = listOf(folder1, folder2)
        setData { copy(folders = folders, isRefreshing = false) }
    }

    private fun loadShareData() = executeWithData { data ->
        if (data.selectedShareType.isEmpty()) {
            return@executeWithData setData { copy(isRefreshing = false) }
        }
        val list = appDatabase.shareDao().getByShareType(data.selectedShareType)
        setData { copy(shareList = list, isRefreshing = false) }
    }

    fun reload() = runWithData { data ->
        if (data.selectedShareType.isEmpty()) {
            loadFolders()
        } else {
            loadShareData()
        }
    }

    fun onFolderClick(position: Int) = runWithData { data ->
        val folder = data.folders.getOrNull(position) ?: return@runWithData
        setData { copy(selectedShareType = folder.shareType, isRefreshing = true) }
        loadShareData()
    }

    fun handleBackPressed(): Boolean {
        return withData { data ->
            if (data.selectedShareType.isEmpty()) {
                return@withData false
            }
            setData { copy(selectedShareType = "", shareList = emptyList()) }
            true
        }
    }
}