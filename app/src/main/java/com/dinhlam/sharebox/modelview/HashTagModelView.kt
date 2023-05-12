package com.dinhlam.sharebox.modelview

import android.view.View
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
        view: View, private val onClick: (String) -> Unit
    ) : BaseListAdapter.BaseViewHolder<HashTagModelView, ModelViewHashtagBinding>(view) {

        override fun onCreateViewBinding(view: View): ModelViewHashtagBinding {
            return ModelViewHashtagBinding.bind(view)
        }

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
