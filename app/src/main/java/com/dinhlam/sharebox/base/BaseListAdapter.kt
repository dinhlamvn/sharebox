package com.dinhlam.sharebox.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.utils.Ids
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BaseListAdapter<T : BaseListAdapter.BaseModelView> private constructor(
    private val viewHolderManager: ViewHolderManager
) : ListAdapter<T, BaseListAdapter.BaseViewHolder<T, ViewBinding>>(DiffCallback()) {

    init {
        setHasStableIds(true)
    }

    abstract class ModelViewsFactory {

        private val coroutineScope = CoroutineScope(Dispatchers.IO)
        private var job: Job? = null

        private val modelViews = mutableListOf<BaseModelView>()

        private var adapter: BaseListAdapter<BaseModelView>? = null

        fun attach(adapter: BaseListAdapter<BaseModelView>) {
            this.adapter = adapter
            requestBuildModelViews()
        }

        fun detach() {
            this.adapter = null
        }

        protected abstract fun buildModelViews()

        fun addModelView(modelView: BaseModelView) {
            modelViews.add(modelView)
        }

        private fun buildModelViewsInternal(coroutineScope: CoroutineScope) {
            modelViews.clear()
            buildModelViews()
            coroutineScope.launch(Dispatchers.Main) {
                adapter?.submitList(modelViews.toList())
            }
        }

        fun requestBuildModelViews(delayMills: Long = 0L) {
            if (job?.isActive == true && job?.isCompleted == false) {
                job?.cancel()
                job = null
            }
            job = coroutineScope.launch {
                delay(delayMills)
                buildModelViewsInternal(this)
            }
        }
    }

    class ViewHolderManager internal constructor() {
        private val viewHolders = mutableMapOf<Int, View.() -> BaseViewHolder<*, ViewBinding>>()

        fun withViewType(viewType: Int, block: View.() -> BaseViewHolder<*, *>) {
            val viewHolderBlock = block.cast<View.() -> BaseViewHolder<*, ViewBinding>>()
                ?: error("Just support view holder extend from ${BaseViewHolder::class}")
            viewHolders[viewType] = viewHolderBlock
        }

        fun getViewHolder(viewType: Int) = viewHolders[viewType]
    }

    companion object {
        @JvmStatic
        fun createAdapter(
            block: ViewHolderManager.() -> Unit
        ): BaseListAdapter<BaseModelView> {
            val viewHolderManager = ViewHolderManager().apply(block)
            return BaseListAdapter(viewHolderManager)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<T, ViewBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewType, parent, false)
        val block = viewHolderManager.getViewHolder(viewType)
            ?: error("ViewHolder of $viewType is undefined.")
        return block.invoke(view).cast() ?: error("Error create view holder of $viewType")
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
        return getItem(position).modelLayoutRes
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).modelId
    }

    fun getModelAtPosition(position: Int): T? {
        return currentList.getOrNull(position)
    }

    abstract class BaseModelView private constructor() {

        constructor(id: String) : this() {
            modelId = Ids.hashString64Bit(id)
        }

        constructor(id: Long) : this() {
            modelId = Ids.hashLong64Bit(id)
        }

        var modelId: Long = 0L
            private set

        abstract val modelLayoutRes: Int

        abstract fun areItemsTheSame(other: BaseModelView): Boolean

        abstract fun areContentsTheSame(other: BaseModelView): Boolean

        open fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig =
            BaseSpanSizeLookup.SpanSizeConfig.Normal

        fun addTo(factory: ModelViewsFactory) {
            factory.addModelView(this)
        }

        fun addToIf(block: () -> Boolean, factory: ModelViewsFactory) {
            if (block.invoke()) {
                factory.addModelView(this)
            }
        }
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
