package com.dinhlam.sharebox.modelview.list

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewListTextBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.utils.Icons

data class ListTextModelView(
    val shareId: String,
    val content: String?,
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
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListTextViewHolder(ModelViewListTextBinding.inflate(inflater, container, false))
    }

    class ShareListTextViewHolder(
        binding: ModelViewListTextBinding,
    ) : BaseListAdapter.BaseViewHolder<ListTextModelView, ModelViewListTextBinding>(
        binding
    ) {

        init {
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 16)
            })
            binding.imageQuoteLeft.setImageDrawable(Icons.quoteLeftIcon(buildContext))
            binding.imageQuoteRight.setImageDrawable(Icons.quoteRightIcon(buildContext))
            binding.imageShare.setImageDrawable(Icons.shareIcon(buildContext))
        }

        override fun onBind(model: ListTextModelView, position: Int) {
            binding.textUserName.text = model.userDetail.name
            ImageLoader.INSTANCE.load(
                buildContext, model.userDetail.avatar, binding.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            binding.container.setOnClickListener {
                model.actionOpen.prop?.invoke(model.shareId)
            }

            binding.imageShare.setOnClickListener {
                model.actionShareToOther.prop?.invoke(model.shareId)
            }

            binding.textShare.text = model.content

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_general)

            binding.textBoxName.setOnClickListener {
                model.actionBoxClick.prop?.invoke(model.boxDetail)
            }
        }

        override fun onUnBind() {
            binding.textShare.text = null
            releaseUI()
        }

        private fun releaseUI() {
            ImageLoader.INSTANCE.release(buildContext, binding.imageAvatar)
        }
    }
}
