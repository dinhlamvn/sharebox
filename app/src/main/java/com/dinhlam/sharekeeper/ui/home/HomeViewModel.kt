package com.dinhlam.sharekeeper.ui.home

import com.dinhlam.sharekeeper.base.BaseViewModel
import com.dinhlam.sharekeeper.database.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val appDatabase: AppDatabase) :
    BaseViewModel<HomeData>(HomeData()) {

    init {
        loadData()
    }

    private fun loadData() = execute {
        setData { copy(isRefreshing = true) }
        delay(500)
        val list = appDatabase.shareDao().getAll()
        setData { copy(shareList = list, isRefreshing = false) }
    }

    fun reload() {
        setData { copy(isRefreshing = true) }
        loadData()
    }
}