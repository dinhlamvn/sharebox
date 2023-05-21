package com.dinhlam.sharebox.helper

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.dialog.singlechoice.SingleChoiceBottomSheetDialogFragment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkHelper @Inject constructor(
    @ApplicationContext context: Context
) {

    fun showOptionMenu(fragmentManager: FragmentManager, items: Array<String>, args: Bundle) {
        SingleChoiceBottomSheetDialogFragment().apply {
            arguments = bundleOf(
                AppExtras.EXTRA_CHOICE_ITEMS to items
            ).apply { putAll(args) }
        }.show(fragmentManager, "SingleChoiceBottomSheetDialogFragment")
    }
}