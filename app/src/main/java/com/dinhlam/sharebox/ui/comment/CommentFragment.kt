package com.dinhlam.sharebox.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DividerItemDecoration
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.FragmentCommentBinding
import com.dinhlam.sharebox.databinding.ModelViewCommentBinding
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.modelview.CommentModelView
import com.dinhlam.sharebox.utils.IconUtils

class CommentFragment : BaseBottomSheetDialogFragment<FragmentCommentBinding>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentCommentBinding {
        return FragmentCommentBinding.inflate(inflater, container, false).apply {
            this.container.updateLayoutParams {
                height = screenHeight().times(0.8f).toInt()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ImageLoader.instance.load(
            requireContext(),
            IconUtils.FAKE_AVATAR,
            viewBinding.imageAvatar
        ) {
            copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
        }

        viewBinding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        viewBinding.recyclerView.adapter = BaseListAdapter.createAdapter({
            repeat(1000) {
                add(
                    CommentModelView(
                        it,
                        "Jack",
                        IconUtils.FAKE_AVATAR,
                        "The quick brown fox jumps over the lazy dog"
                    )
                )
            }
        }) {
            withViewType(R.layout.model_view_comment) {
                CommentModelView.CommentViewHolder(ModelViewCommentBinding.bind(this))
            }
        }.also { it.requestBuildModelViews() }
    }
}