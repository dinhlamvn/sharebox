package com.dinhlam.sharebox.dialog.guideline

import android.net.Uri
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewGuidelineImageBinding
import com.dinhlam.sharebox.imageloader.ImageLoader

data class GuidelineImageModelView(
    val number: Int
) : BaseListAdapter.BaseModelView("guideline_number_$number") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_guideline_image

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class GuidelineImageViewHolder(
        val binding: ModelViewGuidelineImageBinding
    ) : BaseListAdapter.BaseViewHolder<GuidelineImageModelView, ModelViewGuidelineImageBinding>(
        binding
    ) {

        override fun onBind(model: GuidelineImageModelView, position: Int) {
            ImageLoader.instance.load(
                buildContext,
                Uri.parse("file:///android_asset/guideline/${model.number}.png"),
                binding.imageView
            )
        }

        override fun onUnBind() {
        }
    }
}
