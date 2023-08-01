package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewBoxBinding
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.utils.Icons

data class BoxModelView(
    val id: String,
    val name: String,
    val desc: String?,
    val margin: Spacing = Spacing.None,
    val onClick: BaseListAdapter.NoHashProp<View.OnClickListener?> = BaseListAdapter.NoHashProp(null),
) : BaseListAdapter.BaseModelView(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<BoxModelView, ModelViewBoxBinding>(
            ModelViewBoxBinding.inflate(inflater, container, false)
        ) {

            init {
                binding.imageIcon.setImageDrawable(Icons.boxIcon(buildContext))
            }

            override fun onBind(model: BoxModelView, position: Int) {
                binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    marginStart = model.margin.start
                    topMargin = model.margin.top
                    marginEnd = model.margin.end
                    bottomMargin = model.margin.bottom
                }

                binding.container.setOnClickListener(model.onClick.prop)
                binding.textName.text = model.name
                binding.textDesc.text = model.desc
            }

            override fun onUnBind() {

            }
        }
    }
}
