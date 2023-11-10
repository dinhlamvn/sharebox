package com.dinhlam.sharebox.ui.home.general

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.FragmentGeneralBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.dialog.guideline.GuidelineDialogFragment
import com.dinhlam.sharebox.dialog.sharelink.ShareLinkInputDialogFragment
import com.dinhlam.sharebox.dialog.sharetextquote.ShareTextQuoteInputDialogFragment
import com.dinhlam.sharebox.dialog.singlechoice.SingleChoiceBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.modelview.BoxModelView
import com.dinhlam.sharebox.modelview.CarouselModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.MainActionModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreGridLayoutManager
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.LiveEventUtils
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GeneralFragment :
    BaseViewModelFragment<GeneralState, GeneralViewModel, FragmentGeneralBinding>(),
    BoxSelectionDialogFragment.OnBoxSelectedListener,
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    SingleChoiceBottomSheetDialogFragment.OnOptionItemSelectedListener,
    ShareTextQuoteInputDialogFragment.OnShareTextQuoteCallback,
    ShareLinkInputDialogFragment.OnShareLinkCallback {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentGeneralBinding {
        return FragmentGeneralBinding.inflate(inflater, container, false)
    }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val boxId = result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)
                    ?: return@registerForActivityResult
                viewModel.setBox(boxId)
            }
        }

    private val layoutManager by lazy {
        LoadMoreGridLayoutManager(requireContext(), 2, blockShouldLoadMore = {
            return@LoadMoreGridLayoutManager getState(viewModel) { state -> state.canLoadMore && !state.isLoadingMore }
        }) {
            viewModel.loadMores()
        }.apply {
            spanSizeLookup = BaseSpanSizeLookup(shareAdapter, 2)
        }
    }

    private val pickImagesResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData ?: return@registerForActivityResult
                val pickCount =
                    clipData.itemCount.takeIfGreaterThanZero() ?: return@registerForActivityResult
                val intent = if (pickCount == 1) {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        `package` = activity?.packageName
                        putExtra(Intent.EXTRA_STREAM, clipData.getItemAt(0).uri)
                    }
                } else {
                    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "image/*"
                        `package` = activity?.packageName
                        val list = arrayListOf<Uri>()
                        for (i in 0 until pickCount) {
                            list.add(clipData.getItemAt(i).uri)
                        }
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, list)
                    }
                }
                startActivity(intent)
            }
        }

    private val shareAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            MainActionModelView(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                BaseListAdapter.NoHashProp(View.OnClickListener {
                    shareHelper.shareTextQuote(childFragmentManager)
                }),
                BaseListAdapter.NoHashProp(View.OnClickListener {
                    shareHelper.shareLink(childFragmentManager)
                }),
                BaseListAdapter.NoHashProp(View.OnClickListener {
                    pickImagesResultLauncher.launch(router.pickImageIntent(true))
                }),
            ).attachTo(this)

            if (state.isRefreshing) {
                add(LoadingModelView("top_loading"))
            }

            if (state.boxes.isNotEmpty()) {
                SizedBoxModelView(
                    "margin_my_boxes",
                    height = 32.dp(),
                    backgroundColor = android.R.color.transparent
                ).attachTo(this)

                TextModelView(
                    "recently_boxes_title",
                    getString(R.string.recently_boxes),
                    textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle2,
                    height = ViewGroup.LayoutParams.WRAP_CONTENT,
                    gravity = Gravity.START,
                ).attachTo(this)

                val lastIndex = state.boxes.size - 1
                val boxModelViews = state.boxes.mapIndexed { idx, boxDetail ->
                    BoxModelView(
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
                        BaseListAdapter.NoHashProp(::onBoxClicked)
                    )
                }
                add(CarouselModelView("carousel_box", boxModelViews))

                SizedBoxModelView(
                    "margin_bottom_recently_boxes",
                    height = 16.dp(),
                    backgroundColor = android.R.color.transparent
                ).attachTo(this)

                TextModelView(
                    "recently_shares_title",
                    getString(R.string.recently_shares),
                    textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle2,
                    height = ViewGroup.LayoutParams.WRAP_CONTENT,
                    gravity = Gravity.START,
                ).attachTo(this)
            }

            if (state.shares.isEmpty() && !state.isRefreshing) {
                add(
                    TextModelView(
                        "text_empty", getString(R.string.no_result)
                    )
                )
            } else if (state.shares.isNotEmpty()) {
                state.shares.forEach { shareDetail ->
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
                        boxDetail = shareDetail.boxDetail,
                        actionOpen = ::onOpen,
                        actionShareToOther = ::onShareToOther,
                        actionLike = ::onLike,
                        actionComment = ::onComment,
                        actionBookmark = ::onBookmark,
                        actionViewImage = ::viewImage,
                        actionViewImages = ::viewImages,
                        actionBoxClick = ::onBoxClick,

                        ).attachTo(this)

                    //SizedBoxModelView("separator_${shareDetail.shareId}").attachTo(this)
                }

                if (state.isLoadingMore) {
                    LoadingModelView("home_loading_more_${state.currentPage}").attachTo(this)
                }
            }
        }
    }

    private fun onMore(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

        val items = arrayListOf(
            SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(0, getString(R.string.archives)),
            SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(0, getString(R.string.bookmark))
        )

        if (share.isVideoShare) {
            items.add(
                SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(
                    0,
                    getString(R.string.download)
                )
            )
            items.add(
                SingleChoiceBottomSheetDialogFragment.SingleChoiceItem(
                    0,
                    getString(R.string.view_in_source)
                )
            )
        }

        SingleChoiceBottomSheetDialogFragment().apply {
            arguments = bundleOf(
                AppExtras.EXTRA_CHOICE_ITEMS to items.toTypedArray(),
                AppExtras.EXTRA_SHARE_ID to shareId
            )
        }.show(childFragmentManager, "SingleChoiceBottomSheetDialogFragment")
    }

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        getState(viewModel) { state ->
            val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return@getState
            val share =
                state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

            when (position) {
                0 -> shareHelper.shareToOther(share)
                1 -> onBookmark(shareId)
                2 -> WorkerUtils.enqueueDownloadShare(
                    requireContext(),
                    share.shareData.cast<ShareData.ShareUrl>()?.url
                )

                3 -> onOpen(shareId)
            }
        }
    }

    private fun onBoxClick(boxDetail: BoxDetail?) {
        boxDetail?.boxId?.let { boxId -> startActivity(router.boxDetail(requireContext(), boxId)) }
    }

    private fun viewImages(shareId: String, uris: List<Uri>) {
        shareHelper.viewShareImages(requireActivity(), shareId, uris)
    }

    private fun viewImage(shareId: String, uri: Uri) {
        shareHelper.viewShareImage(requireActivity(), shareId, uri)
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var userHelper: UserHelper

    override val viewModel: GeneralViewModel by viewModels()

    override fun onStateChanged(state: GeneralState) {
        shareAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LiveEventUtils.eventScrollToTopGeneral.observe(viewLifecycleOwner) { shouldScroll ->
            if (shouldScroll) {
                viewBinding.recyclerView.smoothScrollToPosition(0)
                viewModel.doOnRefresh()
            }
        }

        viewBinding.toolbar.inflateMenu(R.menu.general_menu)
        viewBinding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_create_box -> createNewBox()
            }
            true
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

        viewModel.consume(this, GeneralState::isLoadingMore) { isLoadMore ->
            layoutManager.hadTriggerLoadMore = isLoadMore
        }

        if (appSharePref.isShowGuideline()) {
            appSharePref.offShowGuideline()
            showGuideline()
        }
    }

    private fun createNewBox() {
        if (!userHelper.isSignedIn()) {
            showToast(R.string.require_sign_in_to_create_box)
            startActivity(router.signIn())
            return
        }

        createBoxResultLauncher.launch(router.boxIntent(requireContext()))
    }

    private fun showGuideline() {
        GuidelineDialogFragment().show(childFragmentManager, "GuidelineDialogFragment")
    }

    private fun onOpen(shareId: String) {
        startActivity(router.shareDetail(requireContext(), shareId))
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onLike(shareId: String) {
        if (!userHelper.isSignedIn()) {
            startActivity(router.signIn())
            return
        }
        viewModel.like(shareId)
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                childFragmentManager, shareId, collectionId
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

    private fun viewInSource(videoSource: VideoSource, shareData: ShareData) {
        shareHelper.viewInSource(requireContext(), videoSource, shareData)
    }

    private fun onBoxClicked(boxId: String) {
        getState(viewModel) { state ->
            state.boxes.firstOrNull { boxDetail -> boxDetail.boxId == boxId }?.let { boxDetail ->
                viewModel.setBox(boxDetail)
            }
        }
    }

    override fun onShareLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            `package` = activity?.packageName
            putExtra(Intent.EXTRA_TEXT, link)
        }
        startActivity(intent)
    }

    override fun onShareTextQuote(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            `package` = activity?.packageName
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(intent)
    }
}