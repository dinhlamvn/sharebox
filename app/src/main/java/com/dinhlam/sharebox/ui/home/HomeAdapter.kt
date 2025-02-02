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
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getDrawableCompat
import com.dinhlam.sharebox.listmodel.BoxListModel
import com.dinhlam.sharebox.listmodel.CircleIconListModel
import com.dinhlam.sharebox.listmodel.DiscoverListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.MainActionListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.TextPairListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import javax.inject.Inject

class HomeAdapter @Inject constructor(
    private val router: Router
) : BaseListAdapter() {
    lateinit var callback: Callback

    interface Callback {
        val buildContext: Context
        val state: HomeState
        fun requestArchiveNote()
        fun requestArchiveWeb()
        fun requestArchiveImages()
        fun requestViewAllBox()
        fun showMore(shareDetail: ShareDetail)
        fun openShare(shareDetail: ShareDetail)
        fun openBox(boxId: String)
        fun editBox(boxId: String)
        fun requestManageMembers(boxId: String)
    }

    override fun buildListModels() {
        val state = callback.state
        MainActionListModel(
            ContextCompat.getColor(callback.buildContext, R.color.md_theme_primary),
            NoHashProp(View.OnClickListener {
                callback.requestArchiveNote()
            }),
            NoHashProp(View.OnClickListener {
                callback.requestArchiveWeb()
            }),
            NoHashProp(View.OnClickListener {
                callback.requestArchiveImages()
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
            text1 = callback.buildContext.getString(R.string.your_boxes),
            textAppearance1 = R.style.TextTitleMedium,
            text2 = callback.buildContext.getString(R.string.view_all),
            textColor2 = R.color.md_theme_primary,
            actionClick2 = NoHashProp(View.OnClickListener {
                callback.requestViewAllBox()
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
                "text_empty_boxes", callback.buildContext.getString(R.string.no_boxes), height = 100.dp()
            ).attachTo(this)
        }

        TextListModel(
            "title_recently",
            text = callback.buildContext.getString(R.string.recently_shares),
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
                callback.buildContext.getString(R.string.no_result),
                height = 100.dp()
            ).attachTo(this)
        } else {
            state.shares.forEachIndexed { idx, share ->
                share.buildListItemListModel(
                    callback::showMore,
                    callback::openShare
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
                FontAwesome.Icon.faw_edit.name,
                callback.buildContext.getString(R.string.title_edit_box)
            ),
            OptionMenuBottomSheetDialogFragment.SingleChoiceItem(
                FontAwesome.Icon.faw_users.name,
                callback.buildContext.getString(R.string.members)
            ),
            OptionMenuBottomSheetDialogFragment.SingleChoiceItem(
                FontAwesome.Icon.faw_copy.name,
                callback.buildContext.getString(R.string.copy_id)
            )
        )
//        OptionMenuBottomSheetDialogFragment.show(
//            fragmentManager,
//            items
//        ) { position, _, _ ->
//            when (position) {
//                0 -> callback.editBox(boxDetail.boxId)
//
//                1 -> callback.requestManageMembers(boxDetail.boxId)
//
//                2 -> callback.context.copy(boxDetail.boxId)
//            }
//        }
    }

    private fun onBoxClick(boxId: String) {
        callback.openBox(boxId)
    }

    private fun getDiscoverList() = buildList {
        add(
            CircleIconListModel(
                "tiktok",
                callback.buildContext.getDrawableCompat(R.drawable.ic_tiktok),
                size = 32.dp(),
                onClick = NoHashProp(View.OnClickListener {
                    callback.buildContext.startActivity(router.tiktokDiscover(callback.buildContext))
                })
            )
        )

        add(
            CircleIconListModel(
                "zing_news",
                callback.buildContext.getDrawableCompat(R.drawable.ic_zing_news),
                size = 32.dp(),
                margin = Spacing.Only(start = 16.dp()),
                onClick = NoHashProp(View.OnClickListener {
                    callback.buildContext.startActivity(router.zingNewsDiscover(callback.buildContext))
                })
            )
        )

        add(
            CircleIconListModel(
                "facebook_downloader",
                Icons.facebookIcon(callback.buildContext),
                size = 32.dp(),
                margin = Spacing.Only(start = 16.dp()),
                onClick = NoHashProp(View.OnClickListener {
                    callback.buildContext.startActivity(router.zingNewsDiscover(callback.buildContext))
                })
            )
        )
    }
}