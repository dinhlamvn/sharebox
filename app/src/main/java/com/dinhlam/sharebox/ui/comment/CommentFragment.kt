package com.dinhlam.sharebox.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetViewModelDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.databinding.FragmentCommentBinding
import com.dinhlam.sharebox.dialog.commentinput.CommentInputDialogFragment
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.modelview.CommentModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

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

    override val viewModel: CommentViewModel by viewModels()

    private val adapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            state.activeUser ?: return@getState run {
                add(LoadingModelView("loading_user"))
            }

            if (state.isRefreshing) {
                add(LoadingModelView("loading_comment"))
                return@getState
            }

            val comments = state.comments
            if (comments.isEmpty()) {
                add(TextModelView("text_empty", "There are no comments yet."))
                return@getState
            } else {
                comments.forEach { comment ->
                    add(
                        CommentModelView(
                            comment.id,
                            comment.userDetail.name,
                            comment.userDetail.avatar,
                            comment.content,
                            comment.createdAt
                        )
                    )
                }
            }
        }
    }

    override fun onStateChanged(state: CommentState) {
        viewBinding.textTitle.text = getString(R.string.comment_title, state.comments.size)
        invalidateUserInfo(state.activeUser)
        adapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.textComment.setOnClickListener {
            CommentInputDialogFragment().show(childFragmentManager, "CommentInputDialogFragment")
        }

        viewBinding.recyclerView.adapter = adapter
        adapter.requestBuildModelViews()

        viewBinding.imageClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun onSubmitComment(comment: String) {
        viewModel.sendComment(comment)
    }

    private fun invalidateUserInfo(userDetail: UserDetail?) {
        val nonNullUser = userDetail ?: return run {
            viewBinding.imageAvatar.isVisible = false
            viewBinding.textComment.isVisible = false
        }

        viewBinding.imageAvatar.isVisible = true
        viewBinding.textComment.isVisible = true

        ImageLoader.instance.load(
            requireContext(), nonNullUser.avatar, viewBinding.imageAvatar
        ) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }
    }

    override fun onConfigBottomSheetBehavior(behavior: BottomSheetBehavior<*>) {
        behavior.peekHeight = 0
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}