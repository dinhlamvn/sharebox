package com.dinhlam.sharebox.ui.sharereceive.modelview

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveImagesBinding
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader

data class ShareReceiveImagesModelView(
    val id: String,
    val uri: Uri,
    val spanCount: Int,
    val width: Int,
    val textNumber: String,
) : BaseListAdapter.BaseModelView("share_multiple_image_$id") {

    override fun createViewHolder(inflater: LayoutInflater, container: ViewGroup): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<ShareReceiveImagesModelView, ModelViewShareReceiveImagesBinding>(
                ModelViewShareReceiveImagesBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: ShareReceiveImagesModelView, position: Int) {
                binding.root.updateLayoutParams {
                    height = model.width
                }

                binding.textNumber.updateLayoutParams {
                    width = model.width
                    height = model.width
                }

                ImageLoader.instance.load(buildContext, model.uri, binding.imageView)

                model.textNumber.takeIfNotNullOrBlank()?.let { textNumber ->
                    binding.textNumber.isVisible = true
                    binding.textNumber.text = textNumber
                } ?: binding.textNumber.apply {
                    text = null
                    isVisible = false
                }
            }

            override fun onUnBind() {
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Custom(spanCount)
    }
}
