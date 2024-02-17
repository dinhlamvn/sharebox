package com.dinhlam.sharebox.listmodel.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ListModelShareTextBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.utils.Icons

data class ShareTextListModel(
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
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
) : BaseListAdapter.BaseListModel(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListTextViewHolder(
            ListModelShareTextBinding.inflate(
                inflater, container, false
            )
        )
    }

    class ShareListTextViewHolder(
        binding: ListModelShareTextBinding,
    ) : BaseListAdapter.BaseViewHolder<ShareTextListModel, ListModelShareTextBinding>(
        binding
    ) {

        init {
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 16)
            })
            binding.imageQuoteLeft.setImageDrawable(Icons.quoteLeftIcon(buildContext))
            binding.imageQuoteRight.setImageDrawable(Icons.quoteRightIcon(buildContext))
            binding.imageAction.setImageDrawable(Icons.moreIcon(buildContext))
        }

        override fun onBind(model: ShareTextListModel, position: Int) {
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

            binding.textShare.text = model.content

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_general)

            binding.textShareDate.text = model.shareDate.format()
        }

        override fun onUnBind() {
            binding.textShare.text = null
            binding.textBoxName.text = null
            binding.textShareDate.text = null
        }
    }
}
