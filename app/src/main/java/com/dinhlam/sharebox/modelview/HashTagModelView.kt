package com.dinhlam.sharebox.modelview

import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewHashtagBinding

data class HashTagModelView(
    val id: String,
    val text: String,
) : BaseListAdapter.BaseModelView("hashtag_$text") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_hashtag

    class HashTagViewHolder(
        val binding: ModelViewHashtagBinding, private val onClick: (String) -> Unit
    ) : BaseListAdapter.BaseViewHolder<HashTagModelView, ModelViewHashtagBinding>(binding) {

        override fun onBind(model: HashTagModelView, position: Int) {
            binding.container.setOnClickListener {
                onClick.invoke(model.id)
            }
            binding.textHashtag.text = "#${model.text}"
        }

        override fun onUnBind() {
        }
    }
}
