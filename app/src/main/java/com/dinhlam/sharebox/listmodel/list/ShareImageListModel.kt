package com.dinhlam.sharebox.listmodel.list

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelShareImageBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.utils.Icons

data class ShareImageListModel(
    val shareId: String,
    val uri: Uri,
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
    val actionViewImage: BaseListAdapter.NoHashProp<(String, Uri) -> Unit> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionBoxClick: BaseListAdapter.NoHashProp<(BoxDetail?) -> Unit> = BaseListAdapter.NoHashProp(
        null
    ),
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
) : BaseListAdapter.BaseListModel(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> = ShareListImageViewHolder(inflater, container)

    private class ShareListImageViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ) : BaseListAdapter.BaseViewHolder<ShareImageListModel, ListModelShareImageBinding>(
        ListModelShareImageBinding.inflate(inflater, container, false)
    ) {

        init {
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 16)
            })
            binding.imageAction.setImageDrawable(Icons.moreIcon(buildContext))
        }

        override fun onBind(model: ShareImageListModel, position: Int) {
            binding.root.updateLayoutParams {
                width = model.width
            }
            binding.root.setOnClickListener {
                model.actionOpen.prop?.invoke(model.shareId)
            }

            binding.textBoxName.setOnClickListener {
                model.actionBoxClick.prop?.invoke(model.boxDetail)
            }

            binding.imageAction.setOnClickListener {
                model.actionShareToOther.prop?.invoke(model.shareId)
            }

            ImageLoader.INSTANCE.load(buildContext, model.uri, binding.imageShare) {
                copy(transformType = TransformType.Normal(ImageLoadScaleType.CenterCrop))
            }

            binding.imageShare.setOnClickListener {
                model.actionViewImage.prop?.invoke(model.shareId, model.uri)
            }

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_general)

            binding.textShareDate.text = model.shareDate.format()
        }

        override fun onUnBind() {
            ImageLoader.INSTANCE.release(buildContext, binding.imageShare)
            binding.textBoxName.text = null
            binding.textShareDate.text = null
        }
    }
}
