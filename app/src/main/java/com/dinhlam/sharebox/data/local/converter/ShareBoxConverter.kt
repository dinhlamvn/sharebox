package com.dinhlam.sharebox.data.local.converter

import androidx.room.TypeConverter
import com.dinhlam.sharebox.data.model.Box
import com.dinhlam.sharebox.utils.BoxUtils

class ShareBoxConverter {

    @TypeConverter
    fun shareBoxToString(shareBox: Box): String {
        return shareBox.id
    }

    @TypeConverter
    fun strToShareBox(str: String): Box {
        return BoxUtils.findBox(str) ?: error("Error parse share box id string $str to Box")
    }
}