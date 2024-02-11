package com.dinhlam.sharebox.ui.comment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetViewModelDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.databinding.FragmentCommentBinding
import com.dinhlam.sharebox.dialog.commentinput.CommentInputDialogFragment
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.listmodel.CommentListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommentFragment :
    BaseBottomSheetViewModelDialogFragment<CommentState, CommentViewModel, FragmentCommentBinding>(),
    CommentInputDialogFragment.OnSubmitCommentCallback {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentCommentBinding {
        return FragmentCommentBinding.inflate(inflater, container, false).apply {
            this.container.updateLayoutParams {
                height = screenHeight().times(0.8f).toInt()
            }
        }
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleSignInResult
    )

    @Inject
    lateinit var router: Router

    override val viewModel: CommentViewModel by viewModels()

    private val adapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                LoadingListModel("loading_comment").attachTo(this)
                return@getState
            }

            val comments = state.comments
            if (comments.isEmpty()) {
                TextListModel("text_empty", "There are no comments yet.").attachTo(this)
                return@getState
            } else {
                comments.forEach { comment ->
                    CommentListModel(
                        comment.id,
                        comment.userDetail.name,
                        comment.userDetail.avatar,
                        comment.content,
                        comment.commentDate
                    ).attachTo(this)
                }
            }
        }
    }

    override fun onStateChanged(state: CommentState) {
        binding.textTitle.text = getString(R.string.comment_title, state.comments.size)
        invalidateUserInfo(state.currentUser)
        adapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageClose.setImageDrawable(Icons.closeIcon(requireContext()) {
            copy(sizeDp = 16)
        })

        binding.textComment.setOnClickListener {
            CommentInputDialogFragment().show(childFragmentManager, "CommentInputDialogFragment")
        }

        binding.recyclerView.adapter = adapter
        adapter.requestBuildModelViews()

        binding.imageClose.setOnClickListener {
            dismissAllowingStateLoss()
        }

        binding.buttonSignIn.setOnClickListener {
            signInLauncher.launch(router.signIn(true))
        }
    }

    override fun onSubmitComment(comment: String) {
        viewModel.sendComment(comment)
    }

    private fun invalidateUserInfo(userDetail: UserDetail?) {
        val nonNullUser = userDetail ?: return run {
            binding.buttonSignIn.isVisible = true
            binding.imageAvatar.isVisible = false
            binding.textComment.isVisible = false
        }

        binding.buttonSignIn.isVisible = false
        binding.imageAvatar.isVisible = true
        binding.textComment.isVisible = true

        ImageLoader.INSTANCE.load(
            requireContext(), nonNullUser.avatar, binding.imageAvatar
        ) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }
    }

    override fun onConfigBottomSheetBehavior(behavior: BottomSheetBehavior<*>) {
        behavior.peekHeight = 0
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun handleSignInResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            viewModel.getCurrentUserProfile()
        }
    }
}