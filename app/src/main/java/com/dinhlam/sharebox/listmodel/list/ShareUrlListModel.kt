package com.dinhlam.sharebox.listmodel.list

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ListModelShareUrlBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.utils.Icons

data class ShareUrlListModel(
    val shareId: String,
    val url: String?,
    val shareDate: Long,
    val shareNote: String?,
    val likeNumber: Int = 0,
    val commentNumber: Int = 0,
    val userDetail: UserDetail,
    val bookmarked: Boolean = false,
    val liked: Boolean = false,
    val boxDetail: BoxDetail?,
    val actionOpen: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionShareToOther: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionLike: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionComment: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionStar: BaseListAdapter.NoHashProp<Function1<String, Unit>> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionBoxClick: BaseListAdapter.NoHashProp<(BoxDetail?) -> Unit> = BaseListAdapter.NoHashProp(
        null
    ),
) : BaseListAdapter.BaseListModel(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListUrlWebHolder(ListModelShareUrlBinding.inflate(inflater, container, false))
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Normal
    }

    private class ShareListUrlWebHolder(
        binding: ListModelShareUrlBinding
    ) : BaseListAdapter.BaseViewHolder<ShareUrlListModel, ListModelShareUrlBinding>(binding) {

        init {
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 13)
            })
            binding.imageShareTo.setImageDrawable(Icons.shareIcon(buildContext))
        }

        override fun onBind(model: ShareUrlListModel, position: Int) {
            binding.root.setOnClickListener {
                model.actionOpen.prop?.invoke(model.shareId)
            }

            binding.textBoxName.setOnClickListener {
                model.actionBoxClick.prop?.invoke(model.boxDetail)
            }

            binding.imageShareTo.setOnClickListener {
                model.actionShareToOther.prop?.invoke(model.shareId)
            }

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_general)
            binding.shareLinkPreview.setLink(model.url)

            binding.textShareDate.text = model.shareDate.format()
        }

        override fun onUnBind() {
            binding.shareLinkPreview.release()
            binding.textBoxName.text = null
            binding.textShareDate.text = null
        }
    }
}
