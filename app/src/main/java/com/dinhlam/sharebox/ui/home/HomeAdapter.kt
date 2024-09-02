package com.dinhlam.sharebox.ui.home

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.dialog.optionmenu.OptionMenuBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.buildListItemListModel
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getDrawableCompat
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.listmodel.BoxListModel
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
import com.dinhlam.sharebox.utils.Icons
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class HomeAdapter @Inject constructor(
    @ActivityContext private val context: Context,
    private val shareHelper: ShareHelper,
    private val router: Router,
) : BaseListAdapter() {
    private val activity: HomeActivity = context.castNonNull()
    private val viewModel: HomeViewModel = activity.viewModel

    override fun buildListModels() = activity.getState(viewModel) { state ->
        MainActionListModel(
            ContextCompat.getColor(activity, R.color.md_theme_primary),
            NoHashProp(View.OnClickListener {
                activity.requestShareText()
            }),
            NoHashProp(View.OnClickListener {
                activity.requestShareWeb()
            }),
            NoHashProp(View.OnClickListener {
                activity.requestShareImages()
            }),
        ).attachTo(this)

        DiscoverListModel(buildList {
            add(
                CircleIconListModel(
                    "tiktok",
                    activity.getDrawableCompat(R.drawable.ic_tiktok),
                    size = 32.dp(),
                    onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        activity.gotoLink("https://tiktok.com")
                    })
                )
            )

            add(
                CircleIconListModel(
                    "youtube",
                    Icons.youtubeIcon(activity),
                    size = 32.dp(),
                    margin = Spacing.Only(start = 16.dp()),
                    onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        activity.gotoLink("https://youtube.com")
                    })
                )
            )

            add(
                CircleIconListModel(
                    "zing_news",
                    activity.getDrawableCompat(R.drawable.ic_zing_news),
                    size = 32.dp(),
                    margin = Spacing.Only(start = 16.dp()),
                    onClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        activity.gotoLink("https://zingnews.vn")
                    })
                )
            )
        }, Spacing.All(16.dp())).attachTo(this)

        if (state.isRefreshing) {
            LoadingListModel("top_loading").attachTo(this)
        }

        VerticalDividerListModel(
            "margin_my_boxes", height = 32.dp(), dividerColor = android.R.color.transparent
        ).attachTo(this)

        TextPairListModel(
            "title_your_boxes",
            text1 = activity.getString(R.string.your_boxes),
            textAppearance1 = R.style.TextTitleMedium,
            text2 = activity.getString(R.string.view_all),
            textColor2 = R.color.md_theme_primary,
            actionClick2 = NoHashProp(View.OnClickListener {
                activity.requestViewAllBox()
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
                    false,
                    NoHashProp(this::onBoxClick),
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
                "text_empty_boxes", activity.getString(R.string.no_boxes), height = 100.dp()
            ).attachTo(this)
        }

        TextListModel(
            "title_recently",
            text = activity.getString(R.string.recently_shares),
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
                "text_empty_shares", activity.getString(R.string.no_result), height = 100.dp()
            ).attachTo(this)
        } else {
            state.shares.forEachIndexed { idx, share ->
                share.buildListItemListModel(
                    activity::showMore,
                    activity::openShare
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
        val items = arrayOf(
            OptionMenuBottomSheetDialogFragment.SingleChoiceItem(
                FontAwesome.Icon.faw_copy.name,
                activity.getString(R.string.copy_id)
            )
        )
        OptionMenuBottomSheetDialogFragment.show(
            activity.supportFragmentManager,
            items
        ) { position, _, _ ->
            when (position) {
                0 -> activity.copy(boxDetail.boxId)
            }
        }
    }

    private fun onBoxClick(boxId: String) {
        activity.startActivity(router.boxDetail(activity, boxId))
    }
}