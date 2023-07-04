package com.dinhlam.sharebox.ui.home.community

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.databinding.FragmentCommunityBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotEmpty
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.extensions.widthPercentage
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.modelview.CommentModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.utils.IconUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommunityFragment :
    BaseViewModelFragment<CommunityState, CommunityViewModel, FragmentCommunityBinding>(),
    BoxSelectionDialogFragment.OnBoxSelectedListener,
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentCommunityBinding {
        return FragmentCommunityBinding.inflate(inflater, container, false)
    }

    private var blockVerifyPasscodeBlock: Function0<Unit>? = null

    private val passcodeConfirmResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                blockVerifyPasscodeBlock?.invoke()
            } else {
                showToast(R.string.error_require_passcode)
            }
            blockVerifyPasscodeBlock = null
        }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = {
            return@LoadMoreLinearLayoutManager getState(viewModel) { state -> state.canLoadMore && !state.isLoadingMore }
        }) {
            viewModel.loadMores()
        }
    }

    private val shareAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView("top_loading"))
            }

            if (state.shares.isEmpty() && !state.isRefreshing) {
                add(
                    TextModelView(
                        "text_empty", getString(R.string.no_result)
                    )
                )
            } else if (state.shares.isNotEmpty()) {
                state.shares.forEach { shareDetail ->
                    add(
                        shareDetail.shareData.buildShareModelViews(
                            screenHeight(),
                            shareDetail.shareId,
                            shareDetail.shareDate,
                            shareDetail.shareNote,
                            shareDetail.user,
                            shareDetail.likeNumber,
                            commentNumber = shareDetail.commentNumber,
                            bookmarked = shareDetail.bookmarked,
                            liked = shareDetail.liked,
                            actionOpen = ::onOpen,
                            actionShareToOther = ::onShareToOther,
                            actionLike = ::onLike,
                            actionComment = ::onComment,
                            actionBookmark = ::onBookmark,
                            actionViewImage = ::viewImage,
                            actionViewImages = ::viewImages
                        )
                    )

                    shareDetail.commentDetail?.let { commentDetail ->
                        CommentModelView(
                            commentDetail.id,
                            commentDetail.userDetail.name,
                            commentDetail.userDetail.avatar,
                            commentDetail.content,
                            commentDetail.commentDate
                        )
                    }?.let { commentModelView ->
                        add(
                            SizedBoxModelView(
                                "divider_comment_${commentModelView.modelId}",
                                height = 1.dp(),
                            )
                        )
                        add(commentModelView)
                    }

                    add(
                        SizedBoxModelView(
                            "divider_${shareDetail.shareId}",
                            height = 8.dp(),
                        )
                    )
                }

                if (state.isLoadingMore) {
                    add(LoadingModelView("home_load_more_${state.currentPage}"))
                }
            }
        }
    }

    private fun viewImages(uris: List<Uri>) {
        shareHelper.viewShareImages(requireActivity(), uris)
    }

    private fun viewImage(uri: Uri) {
        shareHelper.viewShareImage(requireActivity(), uri)
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var appRouter: AppRouter

    override val viewModel: CommunityViewModel by viewModels()

    override fun onStateChanged(state: CommunityState) {
        shareAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.textTitle.setOnClickListener(::showListBoxMenu)

        viewBinding.textTitle.setDrawableCompat(end = IconUtils.boxIcon(requireContext()))

        viewModel.consume(this, CommunityState::currentBox) { currentBox ->
            viewBinding.textTitle.text = currentBox?.boxName ?: getString(R.string.box_community)
        }

        shareAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    layoutManager.scrollToPositionWithOffset(0, 0)
                }
            }
        })

        viewBinding.recyclerView.itemAnimator?.cast<DefaultItemAnimator>()?.supportsChangeAnimations =
            false
        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = shareAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.doOnRefresh()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.consume(this, CommunityState::isLoadingMore) { isLoadMore ->
            layoutManager.hadTriggerLoadMore = isLoadMore
        }
    }

    private fun showListBoxMenu(view: View) = getState(viewModel) { state ->
        val listPopupWindow = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)
        listPopupWindow.anchorView = view
        val boxes = state.boxes.takeIfNotEmpty() ?: return@getState
        val hasViewMore = boxes.size > AppConsts.NUMBER_VISIBLE_BOX

        val boxNames =
            listOf(getString(R.string.box_community)).plus(boxes.take(AppConsts.NUMBER_VISIBLE_BOX)
                .map { box -> box.boxName })
                .run {
                    if (hasViewMore) {
                        plus(getString(R.string.view_more))
                    } else {
                        this
                    }
                }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_popup_window_item, boxNames)
        listPopupWindow.setAdapter(adapter)
        listPopupWindow.setContentWidth(widthPercentage(50))

        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
            listPopupWindow.dismiss()
            if (position == 0) {
                viewModel.setBox(null)
            } else if (hasViewMore && position == boxNames.size - 1) {
                showBoxesDialog()
            } else {
                val box = boxes.getOrNull(position - 1) ?: return@setOnItemClickListener
                val boxPasscode = box.passcode.takeIfNotNullOrBlank()
                    ?: return@setOnItemClickListener viewModel.setBox(box)
                val intent = appRouter.passcodeIntent(requireContext(), boxPasscode)
                intent.putExtra(
                    AppExtras.EXTRA_PASSCODE_DESCRIPTION, getString(
                        R.string.dialog_bookmark_collection_picker_verify_passcode, box.boxName
                    )
                )
                blockVerifyPasscodeBlock = {
                    viewModel.setBox(box)
                }
                passcodeConfirmResultLauncher.launch(intent)
            }
        }

        listPopupWindow.show()
    }

    private fun showBoxesDialog() {
        shareHelper.showBoxSelectionDialog(childFragmentManager)
    }

    private fun onOpen(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> {
                shareHelper.openUrl(
                    requireContext(), shareData.url, appSharePref.isCustomTabEnabled()
                )
            }

            is ShareData.ShareText -> {
                shareHelper.openTextViewerDialog(requireActivity(), shareData.text)
            }

            else -> {}
        }
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onLike(shareId: String) {
        viewModel.like(shareId)
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                childFragmentManager,
                shareId,
                collectionId
            )
        }
    }

    private fun onComment(shareId: String) {
        shareHelper.showCommentDialog(childFragmentManager, shareId)
    }

    override fun onBoxSelected(boxId: String) {
        viewModel.setBox(boxId)
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.bookmark(shareId, bookmarkCollectionId)
    }
}