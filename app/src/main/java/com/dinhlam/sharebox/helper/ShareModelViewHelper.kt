package com.dinhlam.sharebox.helper

import android.content.Context
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.database.entity.User
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.model.ShareDetail
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ShareModelViewHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    fun buildShareModelViews(
        shares: List<ShareDetail>,
        userMap: Map<String, User> = emptyMap()
    ): List<BaseListAdapter.BaseModelView> {
        return shares.mapNotNull { shareDetail ->
            shareDetail.shareData.buildShareModelViews(
                context,
                shareDetail.id,
                shareDetail.createdAt,
                shareDetail.shareNote,
                userMap[shareDetail.userId]
            )
        }
    }
}