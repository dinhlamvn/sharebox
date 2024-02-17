package com.dinhlam.sharebox.listmodel.list

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.PagerSnapHelper
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.databinding.ListModelShareImagesBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.listmodel.ImageListModel
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.recyclerview.decoration.HorizontalCirclePagerItemDecoration
import com.dinhlam.sharebox.utils.Icons

data class ListImagesListModel(
    val shareId: String,
    val uris: List<Uri>,
    val shareDate: Long,
    val shareNote: String?,
    val modelViews: List<ImageListModel>,
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
    val actionViewImages: BaseListAdapter.NoHashProp<(String, List<Uri>) -> Unit> = BaseListAdapter.NoHashProp(
        null
    ),
    val actionBoxClick: BaseListAdapter.NoHashProp<(BoxDetail?) -> Unit> = BaseListAdapter.NoHashProp(
        null
    ),
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
) : BaseListAdapter.BaseListModel(shareId) {

    override fun createViewHolder(
        inflater: LayoutInflater,
        container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ShareListImagesViewHolder(
            ListModelShareImagesBinding.inflate(
                inflater,
                container,
                false
            )
        )
    }

    override fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig {
        return BaseSpanSizeLookup.SpanSizeConfig.Full
    }

    private class ShareListImagesViewHolder(
        binding: ListModelShareImagesBinding,
    ) : BaseListAdapter.BaseViewHolder<ListImagesListModel, ListModelShareImagesBinding>(
        binding
    ) {

        private val adapter = BaseListAdapter.createAdapter {
            models.forEach { it.attachTo(this) }
        }

        private val models = mutableListOf<ImageListModel>()

        init {
            binding.imageAction.setImageDrawable(Icons.moreIcon(buildContext))
            binding.textBoxName.setDrawableCompat(start = Icons.boxIcon(buildContext) {
                copy(sizeDp = 16)
            })
            binding.recyclerViewImage.updateLayoutParams {
                height = buildContext.screenHeight().times(0.5f).toInt()
            }
            PagerSnapHelper().attachToRecyclerView(binding.recyclerViewImage)
            binding.recyclerViewImage.addItemDecoration(
                HorizontalCirclePagerItemDecoration(
                    colorActive = ContextCompat.getColor(buildContext, R.color.colorPrimaryDark)
                )
            )
            binding.recyclerViewImage.adapter = adapter
            adapter.requestBuildModelViews()
        }

        override fun onBind(model: ListImagesListModel, position: Int) {
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

            models.clear()
            models.addAll(model.modelViews)
            adapter.requestBuildModelViews()

            binding.textBoxName.text =
                model.boxDetail?.boxName ?: buildContext.getText(R.string.box_general)

            binding.textShareDate.text = model.shareDate.format()
        }

        override fun onUnBind() {
            models.clear()
            adapter.requestBuildModelViews()
            binding.textBoxName.text = null
            binding.textShareDate.text = null
        }
    }
}
