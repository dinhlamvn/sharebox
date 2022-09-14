package com.dinhlam.sharekeeper.ui.share

import com.dinhlam.sharekeeper.base.BaseViewModel
import com.dinhlam.sharekeeper.database.AppDatabase
import com.dinhlam.sharekeeper.database.entity.Share
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val gson: Gson
) : BaseViewModel<ShareData>(ShareData()) {

    fun setShareInfo(shareInfo: ShareData.ShareInfo) = setData {
        copy(shareInfo = shareInfo)
    }

    fun saveShare(note: String) = executeWithData { myData ->
        if (myData.shareInfo is ShareData.ShareInfo.ShareText) {
            val json = gson.toJson(myData.shareInfo)
            val share = Share(shareType = "text", shareInfo = json, shareNote = note)
            appDatabase.shareDao().insertAll(share)
            setData { copy(isSaveSuccess = true) }
        }
    }
}