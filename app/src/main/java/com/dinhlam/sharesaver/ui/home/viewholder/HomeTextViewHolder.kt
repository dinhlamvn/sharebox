package com.dinhlam.sharesaver.ui.home.viewholder

import android.view.View
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.databinding.ModelViewHomeShareTextBinding
import com.dinhlam.sharesaver.ui.home.modelview.HomeItemModelView
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