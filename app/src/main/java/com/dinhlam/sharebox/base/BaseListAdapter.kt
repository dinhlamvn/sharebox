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
import java.util.concurrent.Executors

class BaseListAdapter<T : BaseListAdapter.BaseModelView> private constructor(
    private val modelViewsBuilder: MutableList<T>.() -> Unit
) : ListAdapter<T, BaseListAdapter.BaseViewHolder<T, ViewBinding>>(DiffCallback()) {

    private val buildModelViewsScope =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    private var buildModelViewsJob: Job? = null

    private val modelViews = mutableListOf<T>()

    private val modelViewsManager = ModelViewsManager<T>()

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

    private class ModelViewsManager<T : BaseModelView> {
        private val modelViewMap = mutableMapOf<Int, T>()

        private var previousModel: T? = null

        fun getModel(viewType: Int) =
            modelViewMap[viewType] ?: error("No model view is provided with view type $viewType")

        fun getViewTypeAndRemember(model: T): Int {
            if (previousModel != null && previousModel!!::class == model::class) {
                return modelViewMap.size
            }

            val viewType = generateViewType(model)
            modelViewMap[viewType] = model
            previousModel = model
            return viewType
        }

        private fun generateViewType(model: T): Int = modelViewMap.size + 1
    }

    companion object {
        @JvmStatic
        fun createAdapter(
            modelViewsBuilder: MutableList<BaseModelView>.() -> Unit,
        ): BaseListAdapter<BaseModelView> {
            return BaseListAdapter(modelViewsBuilder)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<T, ViewBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val model = modelViewsManager.getModel(viewType)
        return model.createViewHolder(inflater).castNonNull()
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
        return modelViewsManager.getViewTypeAndRemember(getItem(position))
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

        abstract fun createViewHolder(
            inflater: LayoutInflater
        ): BaseViewHolder<*, *>

        open fun areItemsTheSame(other: BaseModelView): Boolean {
            return this.modelId == other.modelId
        }

        open fun areContentsTheSame(other: BaseModelView): Boolean {
            return this === other
        }

        open fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig =
            BaseSpanSizeLookup.SpanSizeConfig.Normal
    }

    abstract class BaseViewHolder<T : BaseModelView, VB : ViewBinding>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root) {

        protected val buildContext: Context = itemView.context

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
