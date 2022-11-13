package com.dinhlam.sharesaver.dialog.tag

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseViewModelDialogFragment
import com.dinhlam.sharesaver.databinding.DialogListBinding
import com.dinhlam.sharesaver.databinding.SingleChooseTagBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.setupWith
import com.dinhlam.sharesaver.utils.ExtraUtils
import com.dinhlam.sharesaver.utils.Tags

class ChoiceTagDialogFragment :
    BaseViewModelDialogFragment<ChoiceTagState, ChoiceTagViewModel, DialogListBinding>() {

    interface OnTagSelectedListener {
        fun onTagSelected(tagId: Int)
    }

    private val modelViewsFactory = object : BaseListAdapter.ModelViewsFactory() {
        override fun buildModelViews() = withState(viewModel) { state ->
            Tags.tags.forEachIndexed { index, tag ->
                TagModelView(tag.name, tag.color, index == state.selectedPosition).addTo(this)
            }
        }
    }

    private val adapter = BaseListAdapter.createAdapter {
        withViewType(R.layout.single_choose_tag) {
            TagViewHolder(this) { position ->
                viewModel.selectedPosition(position)
            }
        }
    }


    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogListBinding {
        return DialogListBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.setupWith(adapter, modelViewsFactory)

        arguments?.let { bundle ->
            val title = bundle.getString(ExtraUtils.EXTRA_TITLE)
            val position = bundle.getInt(ExtraUtils.EXTRA_POSITION, -1)
            viewModel.setTitleAndSelectedPosition(title, position)
        }
    }

    override val viewModel: ChoiceTagViewModel by viewModels()

    override fun onStateChanged(state: ChoiceTagState) {
        modelViewsFactory.requestBuildModelViews()
        viewBinding.textView.text = state.title
        viewBinding.textView.isVisible = !state.title.isNullOrEmpty()
    }

    private data class TagModelView(
        val name: String, val color: Int, val selected: Boolean = false
    ) : BaseListAdapter.BaseModelView(name) {
        override val modelLayoutRes: Int
            get() = R.layout.single_choose_tag

        override fun areItemsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is TagModelView && other.name == this.name
        }

        override fun areContentsTheSame(other: BaseListAdapter.BaseModelView): Boolean {
            return other is TagModelView && other == this
        }
    }

    private class TagViewHolder(view: View, val clickListener: OnClickListener) :
        BaseListAdapter.BaseViewHolder<TagModelView, SingleChooseTagBinding>(view) {

        fun interface OnClickListener {
            fun onClick(position: Int)
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
                clickListener.onClick(position)
            }
        }

        override fun onUnBind() {

        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        withState(viewModel) { state ->
            activity?.cast<OnTagSelectedListener>()?.onTagSelected(state.selectedPosition + 1)
        }
    }

    override fun getSpacing(): Int {
        return 32
    }
}