package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelPinterestPinBinding
import com.dinhlam.sharebox.imageloader.load
import com.dinhlam.sharebox.imageloader.release

data class PinterestPinListModel(
    val id: String,
    val imageUrl: String,
    val title: String,
    val onClick: BaseListAdapter.NoHashProp<OnClickListener>,
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<PinterestPinListModel, ListModelPinterestPinBinding>(
                ListModelPinterestPinBinding.inflate(inflater, container, false)
            ) {
            override fun onBind(model: PinterestPinListModel, position: Int) {
                binding.root.setOnClickListener(model.onClick.prop)
                binding.image.load(binding.root.context, model.imageUrl)
                binding.title.text = model.title
            }

            override fun onUnBind() {
                binding.image.release(binding.root.context)
            }
        }
    }
}
