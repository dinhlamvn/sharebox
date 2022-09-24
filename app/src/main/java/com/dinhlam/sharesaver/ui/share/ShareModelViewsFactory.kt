package com.dinhlam.sharesaver.ui.share

import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.ui.share.modelview.ShareDefaultModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareImageModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareMultipleImageModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareTextModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareWebLinkModelView

class ShareModelViewsFactory(
    private val shareReceiveActivity: ReceiveActivity, private val viewModel: ShareViewModel
) : BaseListAdapter.ModelViewsFactory() {

    override fun buildModelViews() = shareReceiveActivity.withData(viewModel) { data ->
        when (data.shareInfo) {
            is ShareData.ShareInfo.ShareText -> renderShareTextContent(data.shareInfo)
            is ShareData.ShareInfo.ShareWebLink -> renderShareWebLinkContent(data.shareInfo)
            is ShareData.ShareInfo.ShareImage -> renderShareImageContent(data.shareInfo)
            is ShareData.ShareInfo.ShareMultipleImage -> renderShareMultipleImageContent(data.shareInfo)
            else -> renderShareContentDefault()
        }
    }

    private fun renderShareContentDefault() {
        ShareDefaultModelView().addTo(this)
    }

    private fun renderShareWebLinkContent(shareInfo: ShareData.ShareInfo.ShareWebLink) {
        ShareWebLinkModelView("shareText", shareInfo.url).addTo(this)
    }

    private fun renderShareTextContent(shareText: ShareData.ShareInfo.ShareText) {
        ShareTextModelView("shareText", shareText.text).addTo(this)
    }

    private fun renderShareImageContent(shareImage: ShareData.ShareInfo.ShareImage) {
        ShareImageModelView("shareImage", shareImage.uri).addTo(this)
    }

    private fun renderShareMultipleImageContent(shareMultipleImage: ShareData.ShareInfo.ShareMultipleImage) {
        shareMultipleImage.uris.mapIndexed { index, uri ->
            ShareMultipleImageModelView(
                "shareMultipleImage$index", uri
            )
        }.forEach { it.addTo(this) }
    }
}