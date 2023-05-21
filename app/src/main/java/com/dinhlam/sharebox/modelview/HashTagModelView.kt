package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewHashtagBinding

data class HashTagModelView(
    val id: String,
    val text: String,
    val onClick: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(null)
) : BaseListAdapter.BaseModelView("hashtag_$text") {

    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<HashTagModelView, ModelViewHashtagBinding>(
            ModelViewHashtagBinding.inflate(inflater, container, false)
        ) {

            override fun onBind(model: HashTagModelView, position: Int) {
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
