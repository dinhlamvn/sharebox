package com.dinhlam.keepmyshare.ui.home

import com.dinhlam.keepmyshare.base.BaseViewModel
import com.dinhlam.keepmyshare.ui.home.modelview.HomeItemModelView
import kotlinx.coroutines.delay

class HomeViewModel : BaseViewModel<HomeData>(HomeData()) {

    init {
        loadData()
    }

    private fun loadData() {

    }

    fun reload() {
        setData { copy(isRefreshing = true) }
        executeWithData {
            delay(5000)
            val newListItem = (0..99).map {
                HomeItemModelView.HomeTextModelView("text$it", "Hello $it")
            }
            setData { copy(listItem = newListItem, isRefreshing = false) }
        }
    }

}