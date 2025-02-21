package com.dinhlam.sharebox.ui.myinvites.listing

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityMyInvitesListingBinding
import com.dinhlam.sharebox.dialog.optionmenu.BottomSheetOptionsMenuDialogFragment
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.extensions.buildListItemListModel
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.openShare
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyInviteShareListingActivity :
    BaseViewModelActivity<MyInviteShareListingState, MyInviteShareListingViewModel, ActivityMyInvitesListingBinding>() {

    override fun onCreateViewBinding(): ActivityMyInvitesListingBinding {
        return ActivityMyInvitesListingBinding.inflate(layoutInflater)
    }

    override val viewModel: MyInviteShareListingViewModel by viewModels()

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var shareHelper: ShareHelper

    private val shareAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.isLoading) {
                LoadingListModel("top_loading", height = 50.dp).attachTo(this)
            }

            if (state.shares.isEmpty()) {
                TextListModel(
                    "text_empty", getString(R.string.no_result)
                ).attachTo(this)
            } else {
                state.shares.forEachIndexed { idx, shareDetail ->
                    shareDetail.buildListItemListModel(::showMore, ::openShare)
                        .attachTo(this)

                    VerticalDividerListModel(
                        "share_divider_$idx",
                        margin = Spacing.Horizontal(16.dp(), 16.dp())
                    ).attachTo(this)
                }
            }
        }
    }

    override fun onStateChanged(state: MyInviteShareListingState) {
        shareAdapter.requestBuildListModels()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        shareAdapter.attachTo(binding.recyclerView, this)

        viewModel.listen(this)
    }

    private fun showMore(share: ShareDetail) {
        val arrayIcons = arrayOf(
            "f064",
            "f4ad",
            "f0c5",
        )
        val choiceItems =
            resources.getStringArray(R.array.my_invite_share_listing_item_more_action)
                .mapIndexed { index, text ->
                    BottomSheetOptionsMenuDialogFragment.SingleChoiceItem(
                        arrayIcons[index], text
                    )
                }.toTypedArray()

        val listener =
            BottomSheetOptionsMenuDialogFragment.OnOptionItemSelectedListener { position, _, _ ->
                when (position) {
                    0 -> shareHelper.shareToOther(this, share)
                    1 -> TextViewerDialogFragment.showDialog(
                        supportFragmentManager,
                        share.shareNote
                    )

                    2 -> shareHelper.copyShare(this, share)
                }
            }

        BottomSheetOptionsMenuDialogFragment.show(
            supportFragmentManager,
            choiceItems,
            bundleOf(),
            listener
        )
    }

    private fun openShare(shareDetail: ShareDetail) {
        openShare(supportFragmentManager, shareDetail, router, shareHelper)
    }
}