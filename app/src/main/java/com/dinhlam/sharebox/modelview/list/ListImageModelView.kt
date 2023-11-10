package com.dinhlam.sharebox.modelview.list

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewListImageBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.utils.Icons

data class ListImageModelView(
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
) : BaseListAdapter.BaseModelView(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListImageViewHolder(
            ModelViewListImageBinding.inflate(
                inflater, container, false
            )
        )
    }

    private class ShareListImageViewHolder(
        binding: ModelViewListImageBinding,
    ) : BaseListAdapter.BaseViewHolder<ListImageModelView, ModelViewListImageBinding>(
        binding
    ) {

        init {
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 16)
            })
            binding.imageDownload.setImageDrawable(Icons.downloadIcon(buildContext) {
                copy(sizeDp = 20)
            })
        }

        override fun onBind(model: ListImageModelView, position: Int) {
            binding.textUserName.text = model.userDetail.name
            ImageLoader.INSTANCE.load(
                buildContext, model.userDetail.avatar, binding.imageAvatar
            ) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }

            ImageLoader.INSTANCE.load(buildContext, model.uri, binding.imageShare) {
                copy(transformType = TransformType.Normal(ImageLoadScaleType.CenterCrop))
            }

            binding.imageShare.setOnClickListener {
                model.actionViewImage.prop?.invoke(model.shareId, model.uri)
            }

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_general)

            binding.textBoxName.setOnClickListener {
                model.actionBoxClick.prop?.invoke(model.boxDetail)
            }
        }

        override fun onUnBind() {
            ImageLoader.INSTANCE.release(buildContext, binding.imageShare)
            releaseUI()
        }

        private fun releaseUI() {
            ImageLoader.INSTANCE.release(buildContext, binding.imageAvatar)
        }
    }
}
