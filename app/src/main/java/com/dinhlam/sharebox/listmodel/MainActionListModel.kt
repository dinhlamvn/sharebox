package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ViewMainActionBinding

data class MainActionListModel(
    val onNoteClick: BaseListAdapter.NoHashProp<OnClickListener>,
    val onWebClick: BaseListAdapter.NoHashProp<OnClickListener>,
    val onImagesClick: BaseListAdapter.NoHashProp<OnClickListener>,
    val onFileClick: BaseListAdapter.NoHashProp<OnClickListener>,
) : BaseListAdapter.BaseListModel("main_action") {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*> {
        return object :
            BaseListAdapter.BaseViewHolderViewBinding<MainActionListModel, ViewMainActionBinding>(
                ViewMainActionBinding.inflate(inflater, container, false)
            ) {
            override fun onBind(model: MainActionListModel, position: Int) {
                binding.buttonArchiveText.setOnClickListener(model.onNoteClick.prop)
                binding.buttonArchiveWeb.setOnClickListener(model.onWebClick.prop)
                binding.buttonArchiveImages.setOnClickListener(model.onImagesClick.prop)
                binding.buttonArchiveFile.setOnClickListener(model.onFileClick.prop)
            }

            override fun onUnBind() {

            }
        }
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }
}