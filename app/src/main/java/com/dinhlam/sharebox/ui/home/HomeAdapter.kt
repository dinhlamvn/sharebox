package com.dinhlam.sharebox.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.dialog.sharelink.ShareLinkInputDialogFragment
import com.dinhlam.sharebox.dialog.sharetextquote.ShareTextQuoteInputDialogFragment
import com.dinhlam.sharebox.dialog.singlechoice.SingleChoiceBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.modelview.BoxListModel
import com.dinhlam.sharebox.modelview.CarouselListModel
import com.dinhlam.sharebox.modelview.LoadingListModel
import com.dinhlam.sharebox.modelview.MainActionListModel
import com.dinhlam.sharebox.modelview.SizedBoxListModel
import com.dinhlam.sharebox.modelview.TextListModel
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.WorkerUtils

class HomeAdapter(
    private val activity: HomeActivity,
    private val viewModel: HomeViewModel,
    private val shareHelper: ShareHelper,
    private val router: Router,
    private val userHelper: UserHelper,
) : BaseListAdapter(), BoxSelectionDialogFragment.OnBoxSelectedListener,
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    SingleChoiceBottomSheetDialogFragment.OnOptionItemSelectedListener,
    ShareTextQuoteInputDialogFragment.OnShareTextQuoteCallback,
    ShareLinkInputDialogFragment.OnShareLinkCallback {

    override fun buildModelViews() = activity.getState(viewModel) { state ->
        MainActionListModel(
            ContextCompat.getColor(activity, R.color.colorPrimary),
            NoHashProp(View.OnClickListener {
                shareHelper.shareTextQuote(activity.supportFragmentManager)
            }),
            NoHashProp(View.OnClickListener {
                shareHelper.shareLink(activity.supportFragmentManager)
            }),
            NoHashProp(View.OnClickListener {
                //pickImagesResultLauncher.launch(router.pickImageIntent(true))
            }),
        ).attachTo(this)

        if (state.isRefreshing) {
            LoadingListModel("top_loading").attachTo(this)
        }

        if (state.boxes.isNotEmpty()) {
            SizedBoxListModel(
                "margin_my_boxes", height = 32.dp(), backgroundColor = android.R.color.transparent
            ).attachTo(this)

            TextListModel(
                "title_your_boxes",
                activity.getString(R.string.your_boxes),
                textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle2,
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity = Gravity.START,
            ).attachTo(this)

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
                    boxDetail.boxId == state.currentBox?.boxId,
                    NoHashProp(::onBoxClicked)
                )
            }
            CarouselListModel("carousel_box", boxModelViews).attachTo(this)

            SizedBoxListModel(
                "margin_bottom_recently_boxes",
                height = 16.dp(),
                backgroundColor = android.R.color.transparent
            ).attachTo(this)

            TextListModel(
                "recently_shares_title",
                activity.getString(R.string.recently_shares),
                textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle2,
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity = Gravity.START,
            ).attachTo(this)
        }

        if (state.shares.isEmpty() && !state.isRefreshing) {
            TextListModel("text_empty", activity.getString(R.string.no_result)).attachTo(this)
        } else if (state.shares.isNotEmpty()) {
            state.shares.forEach { shareDetail ->
                shareDetail.shareData.buildShareModelViews(
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
                    actionShareToOther = ::onShareToOther,
                    actionLike = ::onLike,
                    actionComment = ::onComment,
                    actionBookmark = ::onBookmark,
                    actionViewImage = ::viewImage,
                    actionViewImages = ::viewImages,
                    actionBoxClick = ::onBoxClick
                ).attachTo(this)
            }

            if (state.isLoadingMore) {
                LoadingListModel("home_loading_more_${state.currentPage}").attachTo(this)
            }
        }
    }

    private fun onMore(shareId: String) = activity.getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

        val items = arrayListOf(
            SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(
                0, activity.getString(R.string.archives)
            ), SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(
                0, activity.getString(R.string.bookmark)
            )
        )

        if (share.isVideoShare) {
            items.add(
                SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(
                    0, activity.getString(R.string.download)
                )
            )
            items.add(
                SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(
                    0, activity.getString(R.string.view_in_source)
                )
            )
        }

        SingleChoiceBottomSheetDialogFragment().apply {
            arguments = bundleOf(
                AppExtras.EXTRA_CHOICE_ITEMS to items.toTypedArray(),
                AppExtras.EXTRA_SHARE_ID to shareId
            )
        }.show(
            activity.supportFragmentManager, "SingleChoiceBottomSheetDialogFragment"
        )
    }

    private fun onOpen(shareId: String) {
        activity.startActivity(router.shareDetail(activity, shareId))
    }

    private fun onShareToOther(shareId: String) = activity.getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onLike(shareId: String) {
        if (!userHelper.isSignedIn()) {
            activity.startActivity(router.signIn())
            return
        }
        viewModel.like(shareId)
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                activity.supportFragmentManager, shareId, collectionId
            )
        }
    }

    private fun onComment(shareId: String) {
        shareHelper.showCommentDialog(
            activity.supportFragmentManager, shareId
        )
    }

    override fun onBoxSelected(boxId: String) {
        viewModel.setBox(boxId)
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.bookmark(shareId, bookmarkCollectionId)
    }

    private fun viewInSource(videoSource: VideoSource, shareData: ShareData) {
        shareHelper.viewInSource(activity, videoSource, shareData)
    }

    private fun onBoxClicked(boxId: String) {
        activity.getState(viewModel) { state ->
            state.boxes.firstOrNull { boxDetail -> boxDetail.boxId == boxId }?.let { boxDetail ->
                viewModel.setBox(boxDetail)
            }
        }
    }

    override fun onShareLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            `package` = activity.packageName
            putExtra(Intent.EXTRA_TEXT, link)
        }
        activity.startActivity(intent)
    }

    override fun onShareTextQuote(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            `package` = activity?.packageName
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(intent)
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

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        activity.getState(viewModel) { state ->
            val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return@getState
            val share =
                state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

            when (position) {
                0 -> shareHelper.shareToOther(share)
                1 -> onBookmark(shareId)
                2 -> WorkerUtils.enqueueDownloadShare(
                    activity, share.shareData.cast<ShareData.ShareUrl>()?.url
                )

                3 -> onOpen(shareId)
            }
        }
    }
}