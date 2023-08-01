package com.dinhlam.sharebox.modelview.bookmark

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.ModelViewBookmarkCollectionBinding
import com.dinhlam.sharebox.extensions.asDisplayCountValue
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.utils.Icons

data class BookmarkCollectionModelView(
    val id: String,
    val name: String,
    val thumbnail: String,
    val desc: String,
    val passcode: String?,
    val shareCount: Int,
    val marginStart: Int = 0,
    val marginTop: Int = 0,
    val onClickListener: BaseListAdapter.NoHashProp<OnClickListener> = BaseListAdapter.NoHashProp(
        null
    )
) : BaseListAdapter.BaseModelView("bookmark_collection_$id") {
    override fun createViewHolder(
        inflater: LayoutInflater, container: ViewGroup
    ): BaseListAdapter.BaseViewHolder<*, *> {
        return ModelViewBookmarkCollectionViewHolder(
            ModelViewBookmarkCollectionBinding.inflate(
                inflater, container, false
            )
        )
    }

    private class ModelViewBookmarkCollectionViewHolder(binding: ModelViewBookmarkCollectionBinding) :
        BaseListAdapter.BaseViewHolder<BookmarkCollectionModelView, ModelViewBookmarkCollectionBinding>(
            binding
        ) {

        init {
            binding.imageLock.setImageDrawable(Icons.lockIcon(buildContext) { copy(sizeDp = 20) })
        }

        override fun onBind(model: BookmarkCollectionModelView, position: Int) {
            binding.root.updateLayoutParams<MarginLayoutParams> {
                marginStart = model.marginStart
                topMargin = model.marginTop
            }
            binding.container.setOnClickListener(model.onClickListener.prop)
            ImageLoader.INSTANCE.load(buildContext, model.thumbnail, binding.imageThumbnail)
            binding.textName.text = model.name
            binding.textDesc.text = model.desc
            binding.imageLock.isVisible = !model.passcode.isNullOrBlank()
            binding.textNumber.text = model.shareCount.asDisplayCountValue()
        }

        override fun onUnBind() {

        }
    }
}