package com.dinhlam.sharebox.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.FragmentCommentBinding
import com.dinhlam.sharebox.databinding.ModelViewSingleTextBinding
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.modelview.SingleTextModelView

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
        viewBinding.recyclerView.adapter = BaseListAdapter.createAdapter({
            repeat(1000) {
                add(SingleTextModelView("hello $it", height = ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }) {
            withViewType(R.layout.model_view_single_text) {
                SingleTextModelView.SingleTextViewHolder(ModelViewSingleTextBinding.bind(this))
            }
        }.also { it.requestBuildModelViews() }
    }
}