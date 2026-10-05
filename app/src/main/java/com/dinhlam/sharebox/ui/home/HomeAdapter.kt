package com.dinhlam.sharebox.ui.home

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.extensions.buildListItemListModel
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.listmodel.*
import com.dinhlam.sharebox.model.Spacing
import javax.inject.Inject

class HomeAdapter @Inject constructor(fragment: Fragment) : BaseListAdapter() {
    private val home: HomeFragment = fragment.castNonNull()

    override fun buildListModels() {
        val state = home.viewModel.currentState
        val searching = state.searchQuery.isNotBlank()
        if (state.isRefreshing || state.isSearching) LoadingListModel("library_loading", height = 48.dp).attachTo(this)
        val boxes = if (searching) state.matchingBoxes else state.boxes
        val shares = if (searching) state.matchingShares else state.shares

        if (searching) {
            heading("results", home.getString(R.string.library_search_results))
            if (boxes.isEmpty() && shares.isEmpty() && !state.isSearching) {
                message("no_matches", home.getString(R.string.library_no_matches))
            }
        } else if (boxes.isEmpty() && shares.isEmpty() && !state.isRefreshing) {
            LibraryEmptyListModel(NoHashProp(View.OnClickListener { home.requestArchiveFile() })).attachTo(this)
            return
        } else {
            TextPairListModel("folders", height = 56.dp,
                padding = Spacing.Horizontal(24.dp, 24.dp),
                text1 = home.getString(R.string.library_folders), textAppearance1 = R.style.TextTitleMedium,
                text2 = home.getString(R.string.library_all_folders, state.totalBox),
                textAppearance2 = R.style.TextCaptionMedium, textColor2 = R.color.md_theme_primary,
                actionClick2 = NoHashProp(View.OnClickListener { home.requestViewAllBox() })
            ).attachTo(this)
        }
        boxes.forEach { box ->
            BoxItemListModel("box_${box.boxId}", box.boxId, box.boxName, box.lastSeen,
                hasPasscode = !box.passcode.isNullOrBlank(), isShowOptionAction = true,
                onClick = NoHashProp(View.OnClickListener { home.openBox(box.boxId) }),
                onOptionClick = NoHashProp(View.OnClickListener { home.showBoxOption(box) })
            ).attachTo(this)
        }
        if (!searching) heading("recent", home.getString(R.string.library_recent))
        if (shares.isEmpty() && !searching && !state.isRefreshing) {
            message("no_recent", home.getString(R.string.library_no_recent))
        }
        shares.forEach { share ->
            share.buildListItemListModel(home::showMore, home::openShare).attachTo(this)
        }
    }

    private fun heading(id: String, text: String) {
        TextListModel(id, text, height = 56.dp, gravity = Gravity.START or Gravity.CENTER_VERTICAL,
            textAppearance = R.style.TextTitleMedium, padding = Spacing.Horizontal(24.dp, 24.dp)).attachTo(this)
    }

    private fun message(id: String, text: String) {
        TextListModel(id, text, height = ViewGroup.LayoutParams.WRAP_CONTENT, gravity = Gravity.START,
            padding = Spacing.All(24.dp)).attachTo(this)
    }
}
