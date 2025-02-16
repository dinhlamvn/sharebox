package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.children
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelCheckListBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.ifNotZero
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.view.FontAwesomeIconView

data class CheckListListModel(
    val id: String,
    val title: String,
    val done: Boolean,
    val datetime: Long,
    val reminder: Long,
    val onClickListener: BaseListAdapter.NoHashProp<OnClickListener>,
    val onDoneClickListener: BaseListAdapter.NoHashProp<OnClickListener>,
    val onReminderClickListener: BaseListAdapter.NoHashProp<OnClickListener>,
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return CheckListViewHolder(ListModelCheckListBinding.inflate(inflater, container, false))
    }

    private class CheckListViewHolder(binding: ListModelCheckListBinding) :
        BaseListAdapter.BaseViewHolderViewBinding<CheckListListModel, ListModelCheckListBinding>(
            binding
        ) {
        override fun onBind(model: CheckListListModel, position: Int) {
            binding.root.enableWithAllChildren(!model.done)
            binding.root.setOnClickListener(model.onClickListener.prop)
            binding.textTitle.text = model.title
            binding.iconDone.setOnClickListener(model.onDoneClickListener.prop)
            binding.iconDone.setIconStyle(
                model.done.ifTrue(
                    FontAwesomeIconView.IconStyle.SOLID,
                    FontAwesomeIconView.IconStyle.REGULAR
                )
            )
            binding.iconReminder.setOnClickListener(model.onReminderClickListener.prop)
            binding.iconReminder.setIconStyle(
                model.reminder.ifNotZero.ifTrue(
                    FontAwesomeIconView.IconStyle.SOLID,
                    FontAwesomeIconView.IconStyle.REGULAR
                )
            )
            binding.textDatetime.text =
                model.datetime.ifNotZero.ifTrue(model.datetime.format("dd MMM yyyy, hh:mm a"), "-")
        }

        private fun View.enableWithAllChildren(enable: Boolean) {
            isEnabled = enable
            if (this is ViewGroup) {
                children.forEach { view ->
                    view.enableWithAllChildren(enable)
                }
            }
        }

        override fun onUnBind() {
        }
    }
}
