package com.dinhlam.sharebox.helper

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.dialog.singlechoice.SingleChoiceBottomSheetDialogFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkHelper @Inject constructor() {

    fun showOptionMenu(
        fragmentManager: FragmentManager,
        items: Array<SingleChoiceBottomSheetDialogFragment.SingleChoiceItem>,
        args: Bundle
    ) {
        SingleChoiceBottomSheetDialogFragment().apply {
            arguments = bundleOf(
                AppExtras.EXTRA_CHOICE_ITEMS to items
            ).apply { putAll(args) }
        }.show(fragmentManager, "SingleChoiceBottomSheetDialogFragment")
    }
}