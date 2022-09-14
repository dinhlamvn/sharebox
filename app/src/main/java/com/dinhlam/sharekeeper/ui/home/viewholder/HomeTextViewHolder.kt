package com.dinhlam.sharekeeper.ui.home.viewholder

import android.view.View
import com.dinhlam.sharekeeper.base.BaseListAdapter
import com.dinhlam.sharekeeper.databinding.ModelViewHomeShareTextBinding
import com.dinhlam.sharekeeper.ui.home.modelview.HomeItemModelView
import java.text.SimpleDateFormat
import java.util.Locale

class HomeTextViewHolder(view: View) :
    BaseListAdapter.BaseViewHolder<HomeItemModelView.HomeTextModelView, ModelViewHomeShareTextBinding>(
        view
    ) {

    private val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    override fun onBind(item: HomeItemModelView.HomeTextModelView, position: Int) {
        binding.textView.text = item.text
        binding.textViewCreatedDate.text = df.format(item.createdAt)
    }

    override fun onUnBind() {

    }

    override fun onCreateViewBinding(view: View): ModelViewHomeShareTextBinding {
        return ModelViewHomeShareTextBinding.bind(view)
    }
}