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

    private fun loadFolders() {
        val folder1 = Folder("folder1", "text", "text")
        val folder2 = Folder("folder2", "image", "image")
        val folders = listOf(folder1, folder2)
        setData { copy(folders = folders) }
    }

    fun loadData() = executeWithData { data ->
        if (data.selectedShareType.isEmpty()) {
            setData { copy(isRefreshing = false) }
            return@executeWithData
        }
        val list = appDatabase.shareDao().getByShareType(data.selectedShareType)
        setData { copy(shareList = list, isRefreshing = false) }
    }

    fun reload() {

    }

    fun onFolderClick(position: Int) = runWithData { data ->
        val folder = data.folders.getOrNull(position) ?: return@runWithData
        setData { copy(selectedShareType = folder.shareType, isRefreshing = true) }
    }

    fun handleBackPressed(): Boolean = withData { data ->
        if (data.selectedShareType.isEmpty()) {
            return@withData false
        }
        setData { copy(selectedShareType = "", shareList = emptyList()) }
        true
    }
}