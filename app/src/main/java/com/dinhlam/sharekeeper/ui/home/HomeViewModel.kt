package com.dinhlam.sharekeeper.ui.home

import com.dinhlam.sharekeeper.base.BaseViewModel
import com.dinhlam.sharekeeper.ui.home.modelview.HomeItemModelView
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
                if (it % 2 == 0) {
                    HomeItemModelView.HomeTextModelView("text$it", "Hello $it")
                } else {
                    HomeItemModelView.HomeImageModelView(
                        "image$it",
                        "https://tmdl.edu.vn/wp-content/uploads/2022/07/1640841291_596_hinh-nen-girl-xinh-full-hd-cho-laptop-va-may-1.jpg"
                    )
                }
            }
            setData { copy(listItem = newListItem, isRefreshing = false) }
        }
    }

}