package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewHashtagBinding

data class HashTagListModel(
    val id: String,
    val text: String,
    val onClick: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(null)
) : BaseListAdapter.BaseListModel("hashtag_$text") {

    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<HashTagListModel, ModelViewHashtagBinding>(
            ModelViewHashtagBinding.inflate(inflater, container, false)
        ) {

            override fun onBind(model: HashTagListModel, position: Int) {
                binding.container.setOnClickListener {
                    model.onClick.prop?.invoke(model.id)
                }
                binding.textHashtag.text = "#${model.text}"
            }

            override fun onUnBind() {
            }
        }
    }
}
