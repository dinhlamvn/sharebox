package com.dinhlam.sharebox.listmodel

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelTiktokDiscoverBinding
import com.dinhlam.sharebox.extensions.asViewCount
import com.dinhlam.sharebox.extensions.toHTML

data class TiktokDiscoverListModel(
    val id: String,
    val url: String,
    val desc: String?,
    val views: Int,
    val onClick: BaseListAdapter.NoHashProp<OnClickListener>,
    val onArchiveClick: BaseListAdapter.NoHashProp<OnClickListener>,
    val onDownloadClick: BaseListAdapter.NoHashProp<OnClickListener>,
) : BaseListAdapter.BaseListModel(id) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return object :
            BaseListAdapter.BaseViewHolder<TiktokDiscoverListModel, ListModelTiktokDiscoverBinding>(
                ListModelTiktokDiscoverBinding.inflate(inflater, container, false)
            ) {

            override fun onBind(model: TiktokDiscoverListModel, position: Int) {
                binding.root.setOnClickListener(model.onClick.prop)
                binding.buttonArchive.setOnClickListener(model.onArchiveClick.prop)
                binding.buttonDownload.setOnClickListener(model.onDownloadClick.prop)
                binding.shareLinkPreview.setLink(model.url)
                binding.textDesc.text = model.desc?.toHTML()
                binding.textViews.text =
                    buildContext.getString(R.string.view_count, model.views.asViewCount())
            }

            override fun onUnBind() {

            }
        }
    }
}
