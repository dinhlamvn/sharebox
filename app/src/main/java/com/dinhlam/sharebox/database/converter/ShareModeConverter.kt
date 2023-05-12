package com.dinhlam.sharebox.database.converter

import androidx.room.TypeConverter
import com.dinhlam.sharebox.model.ShareMode
import com.dinhlam.sharebox.utils.ShareUtils

class ShareModeConverter {

    @TypeConverter
    fun shareModeToString(shareMode: ShareMode): String {
        return shareMode.mode
    }

    @TypeConverter
    fun strToShareModel(str: String): ShareMode {
        return when (str) {
            ShareUtils.SHARE_MODE_COMMUNITY -> ShareMode.ShareModeCommunity
            ShareUtils.SHARE_MODE_PERSONAL -> ShareMode.ShareModePersonal
            else -> error("Error parse share mode string $str to ShareMode")
        }
    }
}