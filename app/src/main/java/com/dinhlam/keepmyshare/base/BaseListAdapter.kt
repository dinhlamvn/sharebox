package com.dinhlam.keepmyshare.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dinhlam.keepmyshare.utils.Ids

class BaseListAdapter<T : BaseListAdapter.BaseModelView> private constructor(private val viewHolderFactory: ViewHolderFactory<T, ViewBinding>) :
    ListAdapter<T, BaseListAdapter.BaseViewHolder<T, ViewBinding>>(DiffCallback()) {

    init {
        setHasStableIds(true)
    }

    fun interface ViewHolderFactory<T : BaseModelView, VB : ViewBinding> {
        fun onCreateViewHolder(
            layoutRes: Int,
            inflater: LayoutInflater,
            container: ViewGroup?
        ): BaseViewHolder<T, VB>
    }

    companion object {
        @JvmStatic
        fun <T : BaseModelView, VB : ViewBinding> createAdapter(factory: ViewHolderFactory<T, VB>): BaseListAdapter<BaseModelView> {
            return BaseListAdapter(factory as ViewHolderFactory<BaseModelView, ViewBinding>)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<T, ViewBinding> {
        val inflater = LayoutInflater.from(parent.context)
        return viewHolderFactory.onCreateViewHolder(viewType, inflater, parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T, ViewBinding>, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<T, ViewBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        holder.onBind(getItem(position), position)
    }

    override fun onViewRecycled(holder: BaseViewHolder<T, ViewBinding>) {
        holder.onUnBind()
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).layoutRes
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).modelId
    }

    abstract class BaseModelView() {

        constructor(id: String) : this() {
            modelId = Ids.hashString64Bit(id)
        }

        constructor(id: Long) : this() {
            modelId = Ids.hashLong64Bit(id)
        }

        var modelId: Long = 0L
            private set

        abstract val layoutRes: Int

        abstract fun areItemsTheSame(other: BaseModelView): Boolean

        abstract fun areContentsTheSame(other: BaseModelView): Boolean
    }

    abstract class BaseViewHolder<T : BaseModelView, VB : ViewBinding>(view: View) :
        RecyclerView.ViewHolder(view) {

        protected val context: Context = itemView.context

        protected val binding: VB by lazy { onCreateViewBinding(view) }

        abstract fun onCreateViewBinding(view: View): VB
        abstract fun onBind(item: T, position: Int)
        abstract fun onUnBind()
    }

    class DiffCallback<T : BaseModelView> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return newItem.areItemsTheSame(oldItem)
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return newItem.areContentsTheSame(oldItem)
        }
    }
}