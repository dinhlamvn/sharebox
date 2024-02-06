package com.dinhlam.sharebox.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.utils.Ids
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

abstract class BaseListAdapter constructor(
    private val modelViewsBuilder: (BaseListAdapter.() -> Unit)? = null
) : ListAdapter<BaseListAdapter.BaseModelView, BaseListAdapter.BaseViewHolder<BaseListAdapter.BaseModelView, ViewBinding>>(
    DiffCallback()
), MutableList<BaseListAdapter.BaseModelView> by CopyOnWriteArrayList() {

    abstract fun buildModelViews()

    private val buildModelViewsScope =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    private var buildModelViewsJob: Job? = null

    private val modelViewsManager = ModelViewsManager()

    override fun submitList(list: MutableList<BaseModelView>?) {
        error("No support direct call method")
    }

    override fun submitList(list: MutableList<BaseModelView>?, commitCallback: Runnable?) {
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
        clear()
        modelViewsBuilder?.invoke(this) ?: buildModelViews()
        withContext(Dispatchers.Main) {
            super.submitList(this@BaseListAdapter.toList())
        }
    }

    init {
        super.setHasStableIds(true)
    }

    private class ModelViewsManager {
        private val modelViewTypeMap = mutableMapOf<Int, BaseModelView>()
        private val rememberMap = mutableMapOf<String, Int>()

        fun getModel(viewType: Int) = modelViewTypeMap[viewType]
            ?: error("No model view is provided with view type $viewType")

        fun getViewTypeAndRemember(model: BaseModelView): Int {
            val modelClassName = model::class.java.simpleName
            return rememberMap[modelClassName] ?: let {
                val viewType = generateViewType()
                modelViewTypeMap[viewType] = model
                rememberMap[modelClassName] = viewType
                viewType
            }
        }

        private fun generateViewType(): Int = modelViewTypeMap.size + 1
    }

    companion object {
        @JvmStatic
        fun createAdapter(
            modelViewsBuilder: BaseListAdapter.() -> Unit,
        ): BaseListAdapter {
            return object : BaseListAdapter(modelViewsBuilder) {
                override fun buildModelViews() {
                    // Do-Nothing
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<BaseModelView, ViewBinding> {
        val model = modelViewsManager.getModel(viewType)
        return model.createViewHolder(LayoutInflater.from(parent.context), parent).castNonNull()
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<BaseModelView, ViewBinding>, position: Int
    ) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<BaseModelView, ViewBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        holder.onBind(getItem(position), position)
    }

    override fun onViewRecycled(holder: BaseViewHolder<BaseModelView, ViewBinding>) {
        holder.onUnBind()
    }

    override fun getItemViewType(position: Int): Int {
        return modelViewsManager.getViewTypeAndRemember(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).modelId
    }

    fun getModelAtPosition(position: Int): BaseModelView? {
        return getItem(position)
    }

    class NoHashProp<T>(val prop: T?) {
        override fun equals(other: Any?): Boolean {
            return true
        }

        override fun hashCode(): Int {
            return 0
        }
    }

    abstract class BaseModelView protected constructor(val modelId: Long) {
        constructor(id: String) : this(Ids.hashString64Bit(id))

        abstract fun createViewHolder(
            inflater: LayoutInflater, container: ViewGroup
        ): BaseViewHolder<*, *>

        open fun areItemsTheSame(other: BaseModelView): Boolean {
            return this.modelId == other.modelId
        }

        open fun areContentsTheSame(other: BaseModelView): Boolean {
            return this == other
        }

        open fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig =
            BaseSpanSizeLookup.SpanSizeConfig.Normal

        fun attachTo(adapter: BaseListAdapter) {
            adapter.add(this)
        }

        fun attachToIf(adapter: BaseListAdapter, constraint: () -> Boolean) {
            if (constraint()) {
                adapter.add(this)
            }
        }
    }

    abstract class BaseViewHolder<T : BaseModelView, VB : ViewBinding>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root) {

        protected val buildContext: Context = itemView.context

        abstract fun onBind(model: T, position: Int)
        abstract fun onUnBind()
    }

    class DiffCallback : DiffUtil.ItemCallback<BaseModelView>() {
        override fun areItemsTheSame(oldItem: BaseModelView, newItem: BaseModelView): Boolean {
            return oldItem.areItemsTheSame(newItem)
        }

        override fun areContentsTheSame(oldItem: BaseModelView, newItem: BaseModelView): Boolean {
            return newItem.areContentsTheSame(newItem)
        }
    }
}
