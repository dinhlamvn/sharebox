package com.dinhlam.sharebox.dialog.guideline

import android.net.Uri
import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewGuidelineImageBinding
import com.dinhlam.sharebox.loader.ImageLoader

data class GuidelineImageModelView(
    val number: Int
) : BaseListAdapter.BaseModelView("guideline_number_$number") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_guideline_image

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class GuidelineImageViewHolder(
        view: View
    ) : BaseListAdapter.BaseViewHolder<GuidelineImageModelView, ModelViewGuidelineImageBinding>(view) {

        override fun onCreateViewBinding(view: View): ModelViewGuidelineImageBinding {
            return ModelViewGuidelineImageBinding.bind(view)
        }

        override fun onBind(item: GuidelineImageModelView, position: Int) {
            ImageLoader.load(
                context,
                Uri.parse("file:///android_asset/guideline/${item.number}.png"),
                binding.imageView
            )
        }

        override fun onUnBind() {
        }
    }
}
