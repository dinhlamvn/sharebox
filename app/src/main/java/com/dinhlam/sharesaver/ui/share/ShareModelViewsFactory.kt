package com.dinhlam.sharesaver.ui.share

import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.ui.share.modelview.ShareDefaultModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareImageModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareMultipleImageModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareTextModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareWebLinkModelView

class ShareModelViewsFactory(
    private val shareShareActivity: ShareActivity, private val viewModel: ShareViewModel
) : BaseListAdapter.ModelViewsFactory() {

    override fun buildModelViews() = shareShareActivity.withState(viewModel) { data ->
        when (data.shareInfo) {
            is ShareState.ShareInfo.ShareText -> renderShareTextContent(data.shareInfo)
            is ShareState.ShareInfo.ShareWebLink -> renderShareWebLinkContent(data.shareInfo)
            is ShareState.ShareInfo.ShareImage -> renderShareImageContent(data.shareInfo)
            is ShareState.ShareInfo.ShareMultipleImage -> renderShareMultipleImageContent(data.shareInfo)
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
                "shareMultipleImage$index", uri
            )
        }.forEach { it.addTo(this) }
    }
}