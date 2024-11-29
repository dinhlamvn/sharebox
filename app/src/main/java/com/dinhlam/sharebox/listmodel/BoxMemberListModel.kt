package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelBoxMemberBinding

data class BoxMemberListModel(
    val id: String,
    val memberEmail: String,
    val onTrashClick: BaseListAdapter.NoHashProp<OnClickListener>
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<BoxMemberListModel, ListModelBoxMemberBinding>(
                ListModelBoxMemberBinding.inflate(inflater, container, false)
            ) {
            override fun onBind(model: BoxMemberListModel, position: Int) {
                binding.textEmail.text = model.memberEmail
                binding.imageTrash.setOnClickListener(model.onTrashClick.prop)
            }

            override fun onUnBind() {

            }
        }
    }
}
