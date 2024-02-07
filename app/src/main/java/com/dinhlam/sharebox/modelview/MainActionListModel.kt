package com.dinhlam.sharebox.modelview

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ViewMainActionBinding
import com.dinhlam.sharebox.utils.Icons

data class MainActionListModel(
    val buttonColor: Int? = null,
    val onNoteClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(null),
    val onWebClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(null),
    val onImagesClick: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(null),
) : BaseListAdapter.BaseListModel("main_action") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object : BaseListAdapter.BaseViewHolder<MainActionListModel, ViewMainActionBinding>(
            ViewMainActionBinding.inflate(inflater, container, false)
        ) {

            init {
                binding.buttonArchiveText.setIcon(Icons.noteIcon(buildContext) {
                    copy(colorRes = android.R.color.white)
                })
                binding.buttonArchiveUrl.setIcon(Icons.webIcon(buildContext) {
                    copy(colorRes = android.R.color.white)
                })
                binding.buttonArchiveImages.setIcon(Icons.imageIcon(buildContext) {
                    copy(colorRes = android.R.color.white)
                })
            }

            override fun onBind(model: MainActionListModel, position: Int) {
                buttonColor?.let { color ->
                    binding.buttonArchiveText.setCardBackgroundColor(color)
                    binding.buttonArchiveUrl.setCardBackgroundColor(color)
                    binding.buttonArchiveImages.setCardBackgroundColor(color)
                }

                binding.buttonArchiveText.setOnClickListener(model.onNoteClick.prop)
                binding.buttonArchiveUrl.setOnClickListener(model.onWebClick.prop)
                binding.buttonArchiveImages.setOnClickListener(model.onImagesClick.prop)
            }

            override fun onUnBind() {

            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}