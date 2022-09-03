package com.dinhlam.keepmyshare.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class BaseListAdapter<T : BaseListAdapter.BaseModelView> private constructor() :
    ListAdapter<T, BaseListAdapter.BaseViewHolder>(DiffCallback()) {

    init {
        setHasStableIds(true)
    }

    companion object {
        @JvmStatic
        fun createAdapter(): BaseListAdapter<BaseModelView> {
            return BaseListAdapter()
        }
    }

    @Suppress("unchecked_cast")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BaseViewHolder(inflater.inflate(viewType, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val model = getItem(position)
        model.itemView = holder.itemView
        model.onModelBind(holder, position)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).layoutRes
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).modelId
    }

    abstract class BaseModelView(val modelId: Long) {
        abstract val layoutRes: Int

        lateinit var itemView: View

        abstract fun onModelBind(viewHolder: BaseViewHolder, position: Int)

        override fun hashCode(): Int {
            return modelId.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other !is BaseModelView) {
                return false
            }
            return modelId == other.modelId
        }

        protected fun <T : View> bindView(@IdRes viewId: Int): Lazy<T> {
            return lazy { itemView.findViewById(viewId) }
        }
    }

    class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DiffCallback<T : BaseModelView> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    }
}