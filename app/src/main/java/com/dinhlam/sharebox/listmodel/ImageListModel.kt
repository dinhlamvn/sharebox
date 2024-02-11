package com.dinhlam.sharebox.listmodel

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewImageBinding
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType

data class ImageListModel(
    val uri: Uri,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    val actionClick: BaseListAdapter.NoHashProp<View.OnClickListener?>? = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseListModel("image_model_view_$uri") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<ImageListModel, ModelViewImageBinding>(
            ModelViewImageBinding.inflate(inflater, container, false)
        ) {

            override fun onBind(model: ImageListModel, position: Int) {
                binding.image.updateLayoutParams {
                    height = model.height
                }

                binding.image.setOnClickListener(model.actionClick?.prop)

                ImageLoader.INSTANCE.load(buildContext, model.uri, binding.image) {
                    copy(transformType = TransformType.Normal(ImageLoadScaleType.CenterCrop))
                }
            }

            override fun onUnBind() {
                ImageLoader.INSTANCE.release(buildContext, binding.image)
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
