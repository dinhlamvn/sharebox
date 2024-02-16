package com.dinhlam.sharebox.ui.home

import android.content.Context
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.extensions.buildShareListModel
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.listmodel.BoxListModel
import com.dinhlam.sharebox.listmodel.ButtonListModel
import com.dinhlam.sharebox.listmodel.CarouselListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.MainActionListModel
import com.dinhlam.sharebox.listmodel.SizedBoxListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
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

    override fun buildModelViews() = activity.getState(viewModel) { state ->
        MainActionListModel(
            ContextCompat.getColor(activity, R.color.colorPrimary),
            NoHashProp(View.OnClickListener {
                activity.requestShareText()
            }),
            NoHashProp(View.OnClickListener {
                activity.requestShareLink()
            }),
            NoHashProp(View.OnClickListener {
                activity.requestShareImages()
            }),
        ).attachTo(this)

        if (state.isRefreshing) {
            LoadingListModel("top_loading").attachTo(this)
        }

        SizedBoxListModel(
            "margin_my_boxes", height = 32.dp(), backgroundColor = android.R.color.transparent
        ).attachTo(this)

        TextListModel(
            "title_your_boxes",
            activity.getString(R.string.newest_box),
            textAppearance = R.style.TextTitleMedium,
            height = ViewGroup.LayoutParams.WRAP_CONTENT,
            gravity = Gravity.START,
        ).attachTo(this)

        if (state.boxes.isNotEmpty()) {
            val lastIndex = state.boxes.size - 1
            val boxModelViews = state.boxes.mapIndexed { idx, boxDetail ->
                BoxListModel(
                    "box_${boxDetail.boxId}",
                    boxDetail.boxId,
                    boxDetail.boxName,
                    boxDetail.boxDesc,
                    Spacing.All(
                        if (idx == 0) 16.dp() else 8.dp(),
                        16.dp(),
                        if (idx == lastIndex) 16.dp() else 8.dp(),
                        16.dp()
                    ),
                    !boxDetail.passcode.isNullOrBlank(),
                    false,
                    NoHashProp(::onBoxClicked)
                )
            }
            CarouselListModel("carousel_box", boxModelViews).attachTo(this)
        } else {
            TextListModel(
                "text_empty_boxes", activity.getString(R.string.no_boxes), height = 100.dp()
            ).attachTo(this)
        }

        ButtonListModel(
            "button_choose_box",
            activity.getString(R.string.access_box),
            margin = Spacing.All(16.dp(), 16.dp(), 16.dp(), 16.dp()),
            onClick = NoHashProp(View.OnClickListener {
                activity.requestChooseBox()
            })
        ).attachTo(this)

        SizedBoxListModel(
            "margin_bottom_recently_boxes",
            height = 16.dp(),
            backgroundColor = android.R.color.transparent
        ).attachTo(this)

        TextListModel(
            "recently_shares_title",
            activity.getString(R.string.recently_shares),
            textAppearance = R.style.TextTitleMedium,
            height = ViewGroup.LayoutParams.WRAP_CONTENT,
            gravity = Gravity.START,
        ).attachTo(this)

        if (state.shares.isEmpty() && !state.isRefreshing) {
            TextListModel(
                "text_empty", activity.getString(R.string.no_result), height = 100.dp()
            ).attachTo(this)
        } else if (state.shares.isNotEmpty()) {
            SizedBoxListModel(
                "margin_bottom_recently_boxes",
                height = 16.dp(),
                backgroundColor = android.R.color.transparent
            ).attachTo(this)

            state.shares.forEach { shareDetail ->
                shareDetail.shareData.buildShareListModel(
                    activity.screenHeight(),
                    shareDetail.shareId,
                    shareDetail.shareDate,
                    shareDetail.shareNote,
                    shareDetail.user,
                    shareDetail.likeNumber,
                    commentNumber = shareDetail.commentNumber,
                    bookmarked = shareDetail.bookmarked,
                    liked = shareDetail.liked,
                    boxDetail = shareDetail.boxDetail,
                    actionOpen = ::onOpen,
                    actionViewImage = ::viewImage,
                    actionViewImages = ::viewImages,
                    actionBoxClick = ::onBoxClick,
                    actionShareToOther = ::onShareToOther
                ).attachTo(this)
            }
        }

        LoadingListModel("home_loading_more_${state.currentPage}", height = 100.dp()).attachTo(
            this
        ) { state.canLoadMore }
    }

    private fun onOpen(shareId: String) = activity.getState(viewModel) { state ->
        val share = state.shares.firstOrNull { shareDetail -> shareDetail.shareId == shareId }
            ?: return@getState
        activity.openShare(share)
    }

    private fun onShareToOther(shareId: String) = activity.getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onBoxClicked(boxId: String) {
        activity.startActivity(router.boxDetail(activity, boxId))
    }

    private fun onBoxClick(boxDetail: BoxDetail?) {
        boxDetail?.boxId?.let { boxId -> activity.startActivity(router.boxDetail(activity, boxId)) }
    }

    private fun viewImages(shareId: String, uris: List<Uri>) {
        shareHelper.viewShareImages(activity, shareId, uris)
    }

    private fun viewImage(shareId: String, uri: Uri) {
        shareHelper.viewShareImage(activity, shareId, uri)
    }
}