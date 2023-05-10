package com.dinhlam.sharebox.extensions

import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.modelview.sharelist.ShareListImageModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListWebLinkModelView
import com.dinhlam.sharebox.ui.share.ShareState
import com.dinhlam.sharebox.utils.IconUtils

fun ShareState.ShareInfo.buildShareModelViews(
    shareId: Int,
    createdAt: Long,
    shareNote: String?
): BaseListAdapter.BaseModelView? {
    return when (this) {
        is ShareState.ShareInfo.ShareWebLink -> {
            ShareListWebLinkModelView(
                id = "$shareId",
                iconUrl = IconUtils.getIconUrl(url),
                url = url,
                createdAt = createdAt,
                note = shareNote,
                shareId = shareId
            )
        }

        is ShareState.ShareInfo.ShareImage -> {
            ShareListImageModelView(
                "$shareId", uri, createdAt, shareNote, shareId
            )
        }

        is ShareState.ShareInfo.ShareText -> {
            ShareListTextModelView(
                id = "$shareId",
                iconUrl = IconUtils.getIconUrl(text),
                content = text,
                createdAt = createdAt,
                note = shareNote,
                shareId = shareId
            )
        }

        else -> {
            null
        }
    }
}