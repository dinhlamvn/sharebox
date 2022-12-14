package com.dinhlam.sharebox.dialog.tag

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.databinding.DialogListBinding
import com.dinhlam.sharebox.databinding.SingleChooseTagBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.setupWith
import com.dinhlam.sharebox.utils.ExtraUtils
import com.dinhlam.sharebox.utils.TagUtil

class ChoiceTagDialogFragment :
    BaseViewModelDialogFragment<ChoiceTagState, ChoiceTagViewModel, DialogListBinding>() {

    interface OnTagSelectedListener {
        fun onTagSelected(tagId: Int)
    }

    private val modelViewsFactory = object : BaseListAdapter.ModelViewsFactory() {
        override fun buildModelViews() = getState(viewModel) { state ->
            TagUtil.tags.forEach { tag ->
                TagModelView(
                    tag.id.toLong(),
                    tag.name,
                    tag.color,
                    tag.id == state.selectedTagId
                ).addTo(this)
            }
        }
    }

    private val adapter = BaseListAdapter.createAdapter {
        withViewType(R.layout.single_choose_tag) {
            TagViewHolder(this) { selectedTagId ->
                viewModel.selectedTag(selectedTagId)
            }
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogListBinding {
        return DialogListBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.setupWith(adapter, modelViewsFactory)

        arguments?.let { bundle ->
            val title = bundle.getString(ExtraUtils.EXTRA_TITLE)
            val selectedTag = bundle.getInt(ExtraUtils.EXTRA_POSITION, 0)
            viewModel.setTitleAndSelectedTag(title, selectedTag)
        }
    }

    override val viewModel: ChoiceTagViewModel by viewModels()

    override fun onStateChanged(state: ChoiceTagState) {
        modelViewsFactory.requestBuildModelViews()
        viewBinding.textView.text = state.title
        viewBinding.textView.isVisible = !state.title.isNullOrEmpty()
    }

    private data class TagModelView(
        val id: Long,
        val name: String,
        val color: Int,
        val selected: Boolean = false
    ) : BaseListAdapter.BaseModelView(id) {
        override val modelLayoutRes: Int
            get() = R.layout.single_choose_tag

        override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is TagModelView && other.id == this.id
        }

        override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is TagModelView && other == this
        }
    }

    private class TagViewHolder(view: View, val clickListener: OnClickListener) :
        BaseListAdapter.BaseViewHolder<TagModelView, SingleChooseTagBinding>(view) {

        fun interface OnClickListener {
            fun onClick(tagId: Int)
        }

        override fun onCreateViewBinding(view: View): SingleChooseTagBinding {
            return SingleChooseTagBinding.bind(view)
        }

        override fun onBind(item: TagModelView, position: Int) {
            binding.textView.text = item.name
            binding.cardView.setCardBackgroundColor(item.color)
            binding.viewBackground.setBackgroundColor(
                if (item.selected) {
                    Color.LTGRAY
                } else {
                    Color.WHITE
                }
            )
            binding.root.setOnClickListener {
                clickListener.onClick(item.id.toInt())
            }
        }

        override fun onUnBind() {
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        getState(viewModel) { state ->
            activity?.cast<OnTagSelectedListener>()?.onTagSelected(state.selectedTagId)
        }
    }

    override fun getSpacing(): Int {
        return 32
    }
}
