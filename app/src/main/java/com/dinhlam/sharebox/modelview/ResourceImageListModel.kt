package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewImageBinding
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType

data class ResourceImageListModel(
    @DrawableRes val drawableRes: Int,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    val actionClick: BaseListAdapter.NoHashProp<(() -> Unit)?>? = null,
    val scaleType: ImageLoadScaleType = ImageLoadScaleType.CenterCrop,
) : BaseListAdapter.BaseListModel("image_model_view_$drawableRes") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<ResourceImageListModel, ModelViewImageBinding>(
                ModelViewImageBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: ResourceImageListModel, position: Int) {
                binding.image.updateLayoutParams {
                    height = model.height
                }

                binding.image.setOnClickListener {
                    actionClick?.prop?.invoke()
                }

                ImageLoader.INSTANCE.load(buildContext, model.drawableRes, binding.image) {
                    copy(transformType = TransformType.Normal(model.scaleType))
                }

                when (model.scaleType) {
                    ImageLoadScaleType.FitCenter -> binding.image.scaleType =
                        ImageView.ScaleType.FIT_CENTER

                    else -> binding.image.scaleType = ImageView.ScaleType.CENTER_CROP
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
