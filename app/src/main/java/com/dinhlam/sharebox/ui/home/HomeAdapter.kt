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
import com.dinhlam.sharebox.extensions.getDrawableCompat
import com.dinhlam.sharebox.listmodel.BoxListModel
import com.dinhlam.sharebox.listmodel.ButtonListModel
import com.dinhlam.sharebox.listmodel.CircleIconListModel
import com.dinhlam.sharebox.listmodel.DiscoverListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.MainActionListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.TextPairListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import javax.inject.Inject

class HomeAdapter @Inject constructor(
    private val fragment: Fragment,
    private val router: Router
) : BaseListAdapter() {
    private val homeFragment: HomeFragment = fragment.castNonNull()

    override fun buildListModels() = homeFragment.getState(homeFragment.viewModel) { state ->
        MainActionListModel(
            NoHashProp(View.OnClickListener {
                homeFragment.requestArchiveNote()
            }),
            NoHashProp(View.OnClickListener {
                homeFragment.requestArchiveWeb()
            }),
            NoHashProp(View.OnClickListener {
                homeFragment.requestArchiveImages()
            }),
            NoHashProp(View.OnClickListener {
                homeFragment.requestArchiveFile()
            }),
        ).attachTo(this)

        DiscoverListModel(getDiscoverList(), Spacing.All(16.dp())).attachTo(this)

        if (state.isRefreshing) {
            LoadingListModel("top_loading").attachTo(this)
        }

        VerticalDividerListModel(
            "margin_my_boxes", height = 32.dp(), dividerColor = android.R.color.transparent
        ).attachTo(this)

        TextPairListModel(
            "title_your_boxes",
            text1 = homeFragment.requireContext().getString(R.string.your_boxes),
            textAppearance1 = R.style.TextTitleMedium,
            text2 = homeFragment.requireContext().getString(R.string.view_all, state.totalBox),
            textColor2 = R.color.md_theme_primary,
            actionClick2 = NoHashProp(View.OnClickListener {
                homeFragment.requestViewAllBox()
            })
        ).attachTo(this)

        VerticalDividerListModel(
            "margin_bottom_title_your_boxes",
            height = 16.dp(),
            dividerColor = android.R.color.transparent
        ).attachTo(this)

        if (state.boxes.isNotEmpty()) {
            state.boxes.forEachIndexed { idx, boxDetail ->
                BoxListModel(
                    "box_${boxDetail.boxId}",
                    boxDetail.boxId,
                    boxDetail.boxName,
                    boxDetail.createdDate,
                    Spacing.None,
                    !boxDetail.passcode.isNullOrBlank(),
                    true,
                    NoHashProp(View.OnClickListener {
                        onBoxClick(boxDetail.boxId)
                    }),
                    NoHashProp(View.OnClickListener {
                        onBoxOptionClick(boxDetail)
                    })
                ).attachTo(this)

                VerticalDividerListModel(
                    "box_divider_$idx",
                    margin = Spacing.Only(16.dp())
                ).attachTo(this)
            }
        } else {
            TextListModel(
                "text_empty_boxes",
                homeFragment.requireContext().getString(R.string.no_boxes),
                height = 100.dp()
            ).attachTo(this)
        }

        ButtonListModel(
            "button_create_box",
            "+",
            margin = Spacing.Only(16.dp(), 16.dp(), 16.dp(), 0),
            onClick = NoHashProp(View.OnClickListener {
                homeFragment.requestCreateBox()
            })
        ).attachTo(
            this
        )

        TextListModel(
            "title_recently",
            text = homeFragment.requireContext().getString(R.string.recently_shares),
            height = ViewGroup.LayoutParams.WRAP_CONTENT,
            gravity = Gravity.START,
            textAppearance = R.style.TextTitleMedium,
            padding = Spacing.Only(16.dp(), 16.dp(), 16.dp(), 0)
        ).attachTo(this)

        VerticalDividerListModel(
            "margin_bottom_title_recently",
            height = 16.dp(),
            dividerColor = android.R.color.transparent
        ).attachTo(this)

        if (state.shares.isEmpty()) {
            TextListModel(
                "text_empty_shares",
                homeFragment.requireContext().getString(R.string.no_result),
                height = 100.dp()
            ).attachTo(this)
        } else {
            state.shares.forEachIndexed { idx, share ->
                share.buildListItemListModel(
                    homeFragment::showMore,
                    homeFragment::openShare
                ).attachTo(this)
                VerticalDividerListModel(
                    "share_divider_$idx",
                    margin = Spacing.Only(16.dp())
                ).attachTo(this)
            }
        }

        VerticalDividerListModel(
            "margin_bottom",
            height = 16.dp(),
            dividerColor = android.R.color.transparent
        ).attachTo(this)
    }

    private fun onBoxOptionClick(boxDetail: BoxDetail) {
        homeFragment.showBoxOption(boxDetail)
    }

    private fun onBoxClick(boxId: String) {
        homeFragment.openBox(boxId)
    }

    private fun getDiscoverList() = buildList {
        add(
            CircleIconListModel(
                "tiktok",
                homeFragment.requireContext().getDrawableCompat(R.drawable.ic_tiktok),
                size = 32.dp(),
                onClick = NoHashProp(View.OnClickListener {
                    homeFragment.moveToDiscover(0)
                })
            )
        )

        add(
            CircleIconListModel(
                "zing_news",
                homeFragment.requireContext().getDrawableCompat(R.drawable.ic_zing_news),
                size = 32.dp(),
                margin = Spacing.Only(start = 16.dp()),
                onClick = NoHashProp(View.OnClickListener {
                    homeFragment.moveToDiscover(1)
                })
            )
        )
    }
}