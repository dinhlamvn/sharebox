package com.dinhlam.sharebox.modelview

import android.net.Uri
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewImageBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.loader.ImageLoader

data class ImageModelView(
    val uri: Uri
) : BaseListAdapter.BaseModelView("image_model_view_$uri") {
    override val modelLayoutRes: Int
        get() = R.layout.model_view_image

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    class ImageViewHolder(
        private val binding: ModelViewImageBinding,
    ) : BaseListAdapter.BaseViewHolder<ImageModelView, ModelViewImageBinding>(binding) {

        override fun onBind(model: ImageModelView, position: Int) {
            ImageLoader.load(buildContext, model.uri, binding.image) {
                copy(
                    transformType = ImageLoader.TransformType.Rounded(
                        8.dp(buildContext),
                        ImageLoader.ImageLoadScaleType.FitCenter
                    )
                )
            }
        }

        override fun onUnBind() {
        }
    }
}
