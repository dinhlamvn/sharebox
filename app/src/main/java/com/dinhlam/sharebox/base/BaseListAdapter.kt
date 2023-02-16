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
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class BaseListAdapter<T : BaseListAdapter.BaseModelView> private constructor(
    private val viewHolderManager: ViewHolderManager,
    private val modelViewsBuilder: MutableList<T>.() -> Unit
) : ListAdapter<T, BaseListAdapter.BaseViewHolder<T, ViewBinding>>(DiffCallback()) {

    private val buildModelViewsScope =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    private var buildModelViewsJob: Job? = null

    private val modelViews = mutableListOf<T>()

    override fun submitList(list: MutableList<T>?) {
        error("No support direct call method")
    }

    override fun submitList(list: MutableList<T>?, commitCallback: Runnable?) {
        error("No support direct call method")
    }

    fun requestBuildModelViews() {
        if (buildModelViewsJob?.isActive == true && buildModelViewsJob?.isCompleted == false) {
            buildModelViewsJob?.cancel()
        }

        buildModelViewsJob = buildModelViewsScope.launch {
            buildModelViewsInternal()
        }
    }

    private suspend fun buildModelViewsInternal() {
        modelViews.clear()
        modelViewsBuilder.invoke(modelViews)
        withContext(Dispatchers.Main) {
            super.submitList(modelViews.toList())
        }
    }


    init {
        setHasStableIds(true)
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
            modelViewsBuilder: MutableList<BaseModelView>.() -> Unit,
            block: ViewHolderManager.() -> Unit
        ): BaseListAdapter<BaseModelView> {
            val viewHolderManager = ViewHolderManager().apply(block)
            return BaseListAdapter(viewHolderManager, modelViewsBuilder)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
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
        holder: BaseViewHolder<T, ViewBinding>, position: Int, payloads: MutableList<Any>
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

        open fun areItemsTheSame(other: BaseModelView): Boolean {
            return this.modelId == other.modelId
        }

        open fun areContentsTheSame(other: BaseModelView): Boolean {
            return this === other
        }

        open fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig =
            BaseSpanSizeLookup.SpanSizeConfig.Normal
    }

    abstract class BaseViewHolder<T : BaseModelView, VB : ViewBinding>(view: View) :
        RecyclerView.ViewHolder(view) {

        protected val context: Context = itemView.context

        protected val binding: VB by lazy { onCreateViewBinding(view) }

        abstract fun onCreateViewBinding(view: View): VB
        abstract fun onBind(model: T, position: Int)
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
