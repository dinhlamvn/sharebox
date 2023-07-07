package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewButtonBinding
import com.dinhlam.sharebox.model.Spacing

data class ButtonModelView(
    val id: String,
    val text: String,
    val margin: Spacing = Spacing.None,
    val onClick: BaseListAdapter.NoHashProp<View.OnClickListener?> = BaseListAdapter.NoHashProp(
        null
    )
) : BaseListAdapter.BaseModelView(id) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<ButtonModelView, ModelViewButtonBinding>(
            ModelViewButtonBinding.inflate(inflater, container, false)
        ) {
            override fun onBind(model: ButtonModelView, position: Int) {
                binding.root.updateLayoutParams<MarginLayoutParams> {
                    marginStart = model.margin.start
                    topMargin = model.margin.top
                    marginEnd = model.margin.end
                    bottomMargin = model.margin.bottom
                }

                binding.button.text = model.text
                binding.button.setOnClickListener(model.onClick.prop)
            }

            override fun onUnBind() {

            }
        }
    }
}