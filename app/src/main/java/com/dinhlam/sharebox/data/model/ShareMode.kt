package com.dinhlam.sharebox.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.utils.ShareUtils

sealed class ShareMode(
    val mode: String,
    @DrawableRes val icon: Int,
    @StringRes val text: Int,
    @StringRes val subText: Int
) {
    object ShareModeCommunity : ShareMode(
        ShareUtils.SHARE_MODE_COMMUNITY,
        R.drawable.ic_community,
        R.string.share_community_text,
        R.string.share_community_subtext
    )

    object ShareModePersonal : ShareMode(
        ShareUtils.SHARE_MODE_PERSONAL,
        R.drawable.ic_person,
        R.string.share_personal_text,
        R.string.share_personal_subtext
    )
}
