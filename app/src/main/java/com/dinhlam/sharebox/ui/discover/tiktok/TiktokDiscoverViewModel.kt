package com.dinhlam.sharebox.ui.discover.tiktok

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.network.AppServices
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.TiktokCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TiktokDiscoverViewModel @Inject constructor(
    private val appServices: AppServices,
    private val boxRepository: BoxRepository,
    private val userHelper: UserHelper,
    private val shareRepository: ShareRepository
) : BaseViewModel<TiktokDiscoverState>(TiktokDiscoverState()) {

    init {
        getDefaultBox()
        getTiktokCategories()
        onChange(TiktokDiscoverState::activeCategory) { tiktokCategory ->
            val categoryId = tiktokCategory?.categoryId ?: return@onChange
            getTiktokTrending(categoryId)
        }
    }

    private fun getDefaultBox() {
        suspend {
            boxRepository.findFirst(userHelper.getCurrentUserId())
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    private fun getTiktokCategories() {
        suspend {
            appServices.getTiktokCategories()
        }.execute { asyncLoad ->
            val categories = asyncLoad.data.orEmpty()
            copy(categories = categories, activeCategory = categories.firstOrNull())
        }
    }

    fun refresh() = getState { state ->
        val categoryId = state.activeCategory?.categoryId ?: return@getState
        getTiktokTrending(categoryId)
    }

    private fun getTiktokTrending(categoryId: Int) = suspend {
        appServices.getTiktokTrending(categoryId)
    }.execute { asyncLoad ->
        copy(
            asyncLoadTiktokDiscover = asyncLoad,
            tiktokDiscoverList = asyncLoad.completed.ifTrue(asyncLoad.data, tiktokDiscoverList)
                .orEmpty()
        )
    }

    fun setCurrentBoxId(boxId: String) {
        suspend { boxRepository.findOne(boxId) }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun archiveLink(link: String, boxId: String) {
        suspend {
            val shareData = ShareData.ShareUrl(link)
            shareRepository.insert(
                shareData = shareData,
                shareNote = null,
                shareBoxId = boxId,
                shareUserId = userHelper.getCurrentUserId(),
                isVideoShare = true
            )
            link
        }.execute { asyncLoad ->
            copy(asyncLoadArchive = asyncLoad)
        }
    }

    fun setActiveCategory(tiktokCategory: TiktokCategory) {
        setState { copy(activeCategory = tiktokCategory) }
    }
}