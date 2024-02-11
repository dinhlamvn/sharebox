package com.dinhlam.sharebox.listmodel

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewImageBinding
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.model.Spacing

data class DrawableImageListModel(
    val drawable: Drawable,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    val actionClick: BaseListAdapter.NoHashProp<(() -> Unit)?>? = null,
    val scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
    val margin: Spacing = Spacing.None
) : BaseListAdapter.BaseListModel("image_model_view_$drawable") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<DrawableImageListModel, ModelViewImageBinding>(
                ModelViewImageBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: DrawableImageListModel, position: Int) {
                binding.image.updateLayoutParams<MarginLayoutParams> {
                    width = model.width
                    height = model.height
                    topMargin = model.margin.top
                    marginStart = model.margin.start
                    marginEnd = model.margin.end
                    bottomMargin = model.margin.bottom
                }

                binding.image.setOnClickListener {
                    actionClick?.prop?.invoke()
                }

                binding.image.setImageDrawable(model.drawable)
                binding.image.scaleType = model.scaleType
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
