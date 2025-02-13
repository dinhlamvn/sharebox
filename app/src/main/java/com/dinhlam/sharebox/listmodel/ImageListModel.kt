package com.dinhlam.sharebox.listmodel

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewImageBinding
import com.dinhlam.sharebox.extensions.updateSize
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.imageloader.load
import com.dinhlam.sharebox.imageloader.release

data class ImageListModel(
    val uri: Uri,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    val actionClick: BaseListAdapter.NoHashProp<View.OnClickListener?>? = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseListModel("image_$uri") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<ImageListModel, ModelViewImageBinding>(
                ModelViewImageBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: ImageListModel, position: Int) {
                binding.image.updateSize(model.width, model.height)
                binding.image.setOnClickListener(model.actionClick?.prop)

                binding.image.load(buildContext, model.uri) {
                    copy(transformType = TransformType.Normal(ImageLoadScaleType.FitCenter))
                }
            }

            override fun onUnBind() {
                binding.image.release(buildContext)
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}
