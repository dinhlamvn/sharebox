package com.dinhlam.sharebox.modelview.list

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ModelViewListUrlBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.utils.Icons

data class ListUrlModelView(
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
) : BaseListAdapter.BaseModelView(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListUrlWebHolder(ModelViewListUrlBinding.inflate(inflater, container, false))
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Normal
    }

    private class ShareListUrlWebHolder(
        binding: ModelViewListUrlBinding
    ) : BaseListAdapter.BaseViewHolder<ListUrlModelView, ModelViewListUrlBinding>(
        binding
    ) {

        init {
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 13)
            })
            binding.imageShare.setImageDrawable(Icons.shareIcon(buildContext))
        }

        override fun onBind(model: ListUrlModelView, position: Int) {
            binding.textUserName.text = model.userDetail.name
            ImageLoader.INSTANCE.load(
                buildContext, model.userDetail.avatar, binding.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.imageShare.setOnClickListener {
                model.actionShareToOther.prop?.invoke(model.shareId)
            }

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_general)
            binding.shareLinkPreview.setLink(model.url)
        }

        override fun onUnBind() {
            binding.shareLinkPreview.release()
            releaseUI()
        }

        private fun releaseUI() {
            ImageLoader.INSTANCE.release(buildContext, binding.imageAvatar)
        }
    }
}
