package com.dinhlam.sharebox.ui.share

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.ui.share.modelview.ShareDefaultModelView
import com.dinhlam.sharebox.ui.share.modelview.ShareImageModelView
import com.dinhlam.sharebox.ui.share.modelview.ShareMultipleImageModelView
import com.dinhlam.sharebox.ui.share.modelview.ShareTextModelView
import com.dinhlam.sharebox.ui.share.modelview.ShareWebLinkModelView

class ShareModelViewsFactory(
    private val shareShareActivity: ShareActivity,
    private val viewModel: ShareViewModel
) : BaseListAdapter.ModelViewsFactory() {

    override fun buildModelViews() = shareShareActivity.getState(viewModel) { state ->
        when (state.shareInfo) {
            is ShareState.ShareInfo.ShareText -> renderShareTextContent(state.shareInfo)
            is ShareState.ShareInfo.ShareWebLink -> renderShareWebLinkContent(state.shareInfo)
            is ShareState.ShareInfo.ShareImage -> renderShareImageContent(state.shareInfo)
            is ShareState.ShareInfo.ShareMultipleImage -> renderShareMultipleImageContent(state.shareInfo)
            else -> renderShareContentDefault()
        }
    }

    private fun renderShareContentDefault() {
        ShareDefaultModelView().addTo(this)
    }

    private fun renderShareWebLinkContent(shareInfo: ShareState.ShareInfo.ShareWebLink) {
        ShareWebLinkModelView("shareText", shareInfo.url).addTo(this)
    }

    private fun renderShareTextContent(shareText: ShareState.ShareInfo.ShareText) {
        ShareTextModelView("shareText", shareText.text).addTo(this)
    }

    private fun renderShareImageContent(shareImage: ShareState.ShareInfo.ShareImage) {
        ShareImageModelView("shareImage", shareImage.uri).addTo(this)
    }

    private fun renderShareMultipleImageContent(shareMultipleImage: ShareState.ShareInfo.ShareMultipleImage) {
        shareMultipleImage.uris.mapIndexed { index, uri ->
            ShareMultipleImageModelView(
                "shareMultipleImage$index",
                uri
            )
        }.forEach { it.addTo(this) }
    }
}
