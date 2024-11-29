package com.dinhlam.sharebox.listmodel

import android.net.Uri
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewImageViewMoreBinding
import com.dinhlam.sharebox.extensions.asViewMoreDisplayCountValue
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero
import com.dinhlam.sharebox.imageloader.load
import com.dinhlam.sharebox.imageloader.release

data class ImageViewMoreListModel(
    val uri: Uri,
    val spanCount: Int,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    val number: Int = 0,
    val actionClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(null)
) : BaseListAdapter.BaseListModel("image_view_more_model_view_$uri") {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<ImageViewMoreListModel, ModelViewImageViewMoreBinding>(
                ModelViewImageViewMoreBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: ImageViewMoreListModel, position: Int) {
                binding.root.setOnClickListener(model.actionClick.prop)

                binding.root.updateLayoutParams {
                    width = model.width
                    height = model.height
                }

                binding.textNumber.updateLayoutParams {
                    width = model.width
                    height = model.height
                }

                binding.imageView.load(buildContext, model.uri)

                model.number.takeIfGreaterThanZero()?.let { num ->
                    binding.textNumber.isVisible = true
                    binding.textNumber.text = num.asViewMoreDisplayCountValue()
                } ?: binding.textNumber.apply {
                    text = null
                    isVisible = false
                }
            }

            override fun onUnBind() {
                binding.imageView.release(buildContext)
            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Custom(spanCount)
    }
}
