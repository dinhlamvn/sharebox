package com.dinhlam.sharebox.modelview

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewImageBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType

data class ImageModelView(
    val uri: Uri
) : BaseListAdapter.BaseModelView("image_model_view_$uri") {

    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<ImageModelView, ModelViewImageBinding>(
            ModelViewImageBinding.inflate(inflater, container, false)
        ) {

            override fun onBind(model: ImageModelView, position: Int) {
                ImageLoader.instance.load(buildContext, model.uri, binding.image) {
                    copy(
                        transformType = TransformType.Rounded(
                            8.dp(), ImageLoadScaleType.FitCenter
                        )
                    )
                }
            }

            override fun onUnBind() {
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
