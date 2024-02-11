package com.dinhlam.sharebox.ui.sharedetail

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityShareDetailBinding
import com.dinhlam.sharebox.extensions.buildShareListModel
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.modelview.CommentListModel
import com.dinhlam.sharebox.modelview.LoadingListModel
import com.dinhlam.sharebox.modelview.SizedBoxListModel
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareDetailActivity :
    BaseViewModelActivity<ShareDetailState, ShareDetailViewModel, ActivityShareDetailBinding>() {

    override fun onCreateViewBinding(): ActivityShareDetailBinding {
        return ActivityShareDetailBinding.inflate(layoutInflater)
    }

    private val shareDetailAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            val shareDetail =
                state.share ?: return@getState LoadingListModel("loading").attachTo(this)

            shareDetail.shareData.buildShareListModel(
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
                actionBookmark = ::onBookmark
            ).attachTo(this)

            state.comments.forEach { commentDetail ->
                SizedBoxListModel(
                    "divider_comment_${commentDetail.id}",
                    height = 1.dp(),
                ).attachTo(this)

                CommentListModel(
                    commentDetail.id,
                    commentDetail.userDetail.name,
                    commentDetail.userDetail.avatar,
                    commentDetail.content,
                    commentDetail.commentDate
                ).attachTo(this)
            }
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var appSharePref: AppSharePref

    override val viewModel: ShareDetailViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleSignInResult
    )

    override fun onStateChanged(state: ShareDetailState) {
        shareDetailAdapter.requestBuildModelViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.recyclerView.adapter = shareDetailAdapter
        shareDetailAdapter.requestBuildModelViews()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.onRefresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.imageSend.setImageDrawable(Icons.sendIcon(this))

        binding.imageSend.setOnClickListener {
            val comment = binding.editComment.getTrimmedText()
            viewModel.sendComment(comment)
            binding.editComment.text?.clear()
            binding.editComment.hideKeyboard()
        }

        viewModel.consume(this, ShareDetailState::currentUser) { userDetail ->
            userDetail?.let { user ->
                ImageLoader.INSTANCE.load(
                    this, user.avatar, binding.imageAvatar
                ) {
                    copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
                }
                binding.buttonSignIn.isVisible = false
            } ?: apply {
                binding.buttonSignIn.isVisible = true
            }
        }

        binding.buttonSignIn.setOnClickListener {
            signInLauncher.launch(router.signIn(true))
        }
    }

    private fun onOpen(shareId: String) = getState(viewModel) { state ->
        val share = state.share ?: return@getState
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> {
                shareHelper.openUrl(
                    this, shareData.url, appSharePref.isCustomTabEnabled()
                )
            }

            is ShareData.ShareText -> {
                shareHelper.openTextViewerDialog(this, shareData.text)
            }

            else -> {}
        }
    }


    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share = state.share ?: return@getState
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
                supportFragmentManager, shareId, collectionId
            )
        }
    }

    private fun handleSignInResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            viewModel.getCurrentUserProfile()
        }
    }
}