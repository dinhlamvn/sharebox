package com.dinhlam.sharebox.ui.discover.tiktok

import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.network.AppServices
import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.ShareData
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
        getTiktokTrending()
    }

    private fun getDefaultBox() {
        suspend {
            boxRepository.findFirst(userHelper.getCurrentUserId())
        }.execute { asyncLoad ->
            copy(currentBox = asyncLoad.data)
        }
    }

    fun getTiktokTrending() = suspend {
        appServices.getTiktokTrending()
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
}