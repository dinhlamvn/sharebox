package com.dinhlam.sharebox.ui.home.profile

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.FragmentProfileBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.heightPercentage
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.screenWidth
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.modelview.BoxListModel
import com.dinhlam.sharebox.modelview.ButtonListModel
import com.dinhlam.sharebox.modelview.CarouselListModel
import com.dinhlam.sharebox.modelview.DrawableImageListModel
import com.dinhlam.sharebox.modelview.LoadingListModel
import com.dinhlam.sharebox.modelview.SizedBoxListModel
import com.dinhlam.sharebox.modelview.TextListModel
import com.dinhlam.sharebox.modelview.profile.ProfileInfoListModel
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.LiveEventUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment :
    BaseViewModelFragment<ProfileState, ProfileViewModel, FragmentProfileBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener {

    private val resultLauncherVerifyPasscode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val boxId = result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)
                    ?: return@registerForActivityResult showToast(R.string.error_require_passcode)
                val boxDetail = getState(viewModel) { state ->
                    state.boxes.firstOrNull { boxDetail ->
                        boxDetail.boxId == boxId
                    }
                } ?: return@registerForActivityResult showToast(R.string.error_require_passcode)
                viewModel.setBox(boxDetail)
            } else {
                showToast(R.string.error_require_passcode)
            }
        }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleSignInResult
    )

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = {
            getState(viewModel) { state ->
                state.canLoadMore && !state.isLoadingMore
            }
        }) {
            viewModel.loadMores()
        }
    }

    private val adapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingListModel("loading"))
                return@getState
            }

            val nonNullUser = state.currentUser ?: return@getState run {
                add(
                    TextListModel(
                        "text_sign_in_message",
                        getString(R.string.sign_in_message),
                        height = heightPercentage(70)
                    )
                )

                add(
                    ButtonListModel(
                        "button_sign_in",
                        getString(R.string.sign_in),
                        Spacing.All(16.dp(), 16.dp(), 16.dp(), 16.dp()),
                        BaseListAdapter.NoHashProp(View.OnClickListener {
                            signInLauncher.launch(router.signIn(true))
                        })
                    )
                )

                val margin = screenWidth().minus(48.dp()).div(2)

                add(
                    DrawableImageListModel(
                        Icons.settingIcon(requireContext()),
                        width = 48.dp(),
                        height = 48.dp(),
                        scaleType = ImageView.ScaleType.CENTER_INSIDE,
                        actionClick = BaseListAdapter.NoHashProp {
                            openSettingPage()
                        },
                        margin = Spacing.Horizontal(margin, margin)
                    )
                )
            }

            add(
                ProfileInfoListModel(
                    nonNullUser.id,
                    nonNullUser.avatar,
                    nonNullUser.name,
                    nonNullUser.drama,
                    nonNullUser.level,
                    nonNullUser.joinDate,
                    Icons.dramaIcon(requireContext()),
                    Icons.levelIcon(requireContext()),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        openSettingPage()
                    })
                )
            )
            add(
                SizedBoxListModel("divider", height = 8.dp())
            )

            if (state.boxes.isNotEmpty()) {
                TextListModel(
                    "box_title",
                    getString(R.string.your_boxes),
                    height = 50.dp(),
                    gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                    textAppearance = R.style.TextAppearance_MaterialComponents_Body1
                ).attachTo(this)

                SizedBoxListModel("divider_top_carousel", height = 1.dp()).attachTo(this)

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
                        BaseListAdapter.NoHashProp(::onBoxClicked)
                    )
                }
                add(CarouselListModel("carousel_box", boxModelViews))

                SizedBoxListModel("divider_bottom_carousel", height = 8.dp()).attachTo(this)
            }

            state.currentBox?.let { boxDetail ->
                TextListModel(
                    "active_box_title",
                    boxDetail.boxName,
                    height = 50.dp(),
                    gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                    textAppearance = R.style.TextAppearance_MaterialComponents_Headline6
                ).attachTo(this)
                SizedBoxListModel("divider_active_box_title", height = 1.dp()).attachTo(this)
            }



            if (state.shares.isNotEmpty()) {
                state.shares.map { shareDetail ->
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
                        actionBookmark = ::onBookmark
                    )
                }.forEach { model ->
                    add(model)
                    add(
                        SizedBoxListModel(
                            "divider_${model.modelId}",
                            height = 8.dp(),
                        )
                    )
                }

                if (state.isLoadingMore) {
                    add(LoadingListModel("loading_more_${state.currentPage}"))
                }
            }
        }
    }

    private fun onBoxClicked(boxId: String) {
        getState(viewModel) { state ->
            state.boxes.firstOrNull { boxDetail -> boxDetail.boxId == boxId }?.let { boxDetail ->
                if (boxDetail.passcode.isNullOrBlank()) {
                    viewModel.setBox(boxDetail)
                } else {
                    val bundle = Bundle()
                    bundle.putString(AppExtras.EXTRA_BOX_ID, boxId)
                    resultLauncherVerifyPasscode.launch(
                        router.passcodeIntent(
                            requireContext(),
                            boxDetail.passcode,
                            bundle
                        )
                    )
                }
            }
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var router: Router

    override val viewModel: ProfileViewModel by viewModels()

    override fun onStateChanged(state: ProfileState) {
        adapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LiveEventUtils.eventScrollToTopProfile.observe(viewLifecycleOwner) { shouldScroll ->
            if (shouldScroll) {
                viewBinding.recyclerView.smoothScrollToPosition(0)
            }
        }

        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = adapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewBinding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnRefresh()
        }

        viewModel.consume(this, ProfileState::isLoadingMore) { isLoadMore ->
            layoutManager.hadTriggerLoadMore = isLoadMore
        }
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

    private fun openSettingPage() {
        startActivity(router.settingIntent())
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.bookmark(shareId, bookmarkCollectionId)
    }

    private fun handleSignInResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            viewModel.doOnRefresh()
        }
    }
}