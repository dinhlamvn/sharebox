package com.dinhlam.sharebox.ui.home.community

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.database.entity.Share
import com.dinhlam.sharebox.model.ShareType
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.modelview.sharelist.ShareListImageModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListWebLinkModelView
import com.dinhlam.sharebox.repository.ShareRepository
import com.dinhlam.sharebox.ui.share.ShareState
import com.dinhlam.sharebox.utils.IconUtils
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val gson: Gson
) : BaseViewModel<CommunityState>(CommunityState()) {

    init {
        consume(CommunityState::shareList, true) { shares ->
            setState { copy(shareModelViews = buildShareModelViews(shares)) }
        }
        loadShares()
    }

    fun loadShares() {
        setState { copy(isRefreshing = true) }
        backgroundTask {
            val shares = shareRepository.findAll(sortType = SortType.NEWEST)
            delay(1000)
            setState { copy(shareList = shares, isRefreshing = false) }
        }
    }

    private fun buildShareModelViews(shares: List<Share>): List<BaseListAdapter.BaseModelView> {
        return shares.mapNotNull { share ->
            when (share.shareType) {
                ShareType.WEB.type -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo, ShareState.ShareInfo.ShareWebLink::class.java
                    )
                    ShareListWebLinkModelView(
                        id = "${share.id}",
                        iconUrl = IconUtils.getIconUrl(shareInfo.url),
                        url = shareInfo.url,
                        createdAt = share.createdAt,
                        note = share.shareNote,
                        shareId = share.id
                    )
                }

                ShareType.IMAGE.type -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo, ShareState.ShareInfo.ShareImage::class.java
                    )
                    ShareListImageModelView(
                        "${share.id}", shareInfo.uri, share.createdAt, share.shareNote, share.id
                    )
                }

                ShareType.TEXT.type -> {
                    val shareInfo = gson.fromJson(
                        share.shareInfo, ShareState.ShareInfo.ShareText::class.java
                    )
                    ShareListTextModelView(
                        id = "${share.id}",
                        iconUrl = IconUtils.getIconUrl(shareInfo.text),
                        content = shareInfo.text,
                        createdAt = share.createdAt,
                        note = share.shareNote,
                        shareId = share.id
                    )
                }

                else -> {
                    null
                }
            }
        }
    }
}