package com.dinhlam.sharebox.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharebox.extensions.cast
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

abstract class BaseListAdapter :
    ListAdapter<BaseListAdapter.BaseListModel, BaseListAdapter.BaseViewHolder<BaseListAdapter.BaseListModel, ViewBinding>>(
        DiffCallback()
    ), DefaultLifecycleObserver {

    abstract fun buildListModels()

    private val buildListModelsScope =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    private var recyclerView: RecyclerView? = null

    private var buildListModelsJob: Job? = null

    private val listModelManager = ListModelManager()

    private val listModels: MutableList<BaseListModel> = CopyOnWriteArrayList()

    fun attachTo(recyclerView: RecyclerView, lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
        this.recyclerView = recyclerView
        this.recyclerView?.adapter = this
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        owner.lifecycle.removeObserver(this)
        recyclerView?.adapter = null
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        requestBuildListModels()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        listModelManager.onClear()
        listModels.clear()
        super.submitList(null)
    }

    @Synchronized
    protected fun addModel(baseListModel: BaseListModel) {
        listModels.add(baseListModel)
    }

    override fun submitList(list: MutableList<BaseListModel>?) {
        error("No support direct call method")
    }

    override fun submitList(list: MutableList<BaseListModel>?, commitCallback: Runnable?) {
        error("No support direct call method")
    }

    fun requestBuildListModels(lists: List<BaseListModel>) {
        cancelCurrentBuild()
        buildListModelsJob = buildListModelsScope.launch {
            listModels.clear()
            listModels.addAll(lists)
            withContext(Dispatchers.Main) {
                super.submitList(listModels.toList())
            }
        }
    }

    fun requestBuildListModels() {
        cancelCurrentBuild()

        buildListModelsJob = buildListModelsScope.launch {
            buildListModelsInternal()
        }
    }

    private fun cancelCurrentBuild() {
        if (buildListModelsJob?.isActive == true && buildListModelsJob?.isCompleted == false) {
            buildListModelsJob?.cancel()
        }
    }

    private suspend fun buildListModelsInternal() {
        listModels.clear()
        buildListModels()
        withContext(Dispatchers.Main) {
            super.submitList(listModels.toList())
        }
    }

    init {
        super.setHasStableIds(true)
    }

    private class ListModelManager {
        private val modelViewTypeMap = mutableMapOf<Int, BaseListModel>()
        private val rememberMap = mutableMapOf<String, Int>()

        fun getModel(viewType: Int) = modelViewTypeMap[viewType]
            ?: error("No model view is provided with view type $viewType")

        fun getViewTypeAndRemember(model: BaseListModel): Int {
            val modelClassName = model::class.java.simpleName
            return rememberMap[modelClassName] ?: let {
                val modelViewTypeConfig = model.getViewTypeConfig()
                val viewType =
                    modelViewTypeConfig.cast<BaseListModel.BaseListModelViewTypeConfig.Manual>()?.viewType
                        ?: generateViewType()
                modelViewTypeMap[viewType] = model
                rememberMap[modelClassName] = viewType
                viewType
            }
        }

        private fun generateViewType(): Int = modelViewTypeMap.size + 1

        fun onClear() {
            this.modelViewTypeMap.clear()
            this.rememberMap.clear()
        }
    }

    companion object {
        @JvmStatic
        fun create(
            modelViewsBuilder: BaseListAdapter.() -> Unit,
        ): BaseListAdapter {
            return object : BaseListAdapter() {
                override fun buildListModels() {
                    modelViewsBuilder.invoke(this)
                }
            }
        }

        @JvmStatic
        fun create(): BaseListAdapter {
            return object : BaseListAdapter() {
                override fun buildListModels() {
                    // do-nothing
                    // just build with requestBuildListModels(List<BaseModel>)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<BaseListModel, ViewBinding> {
        val model = listModelManager.getModel(viewType)
        return model.createViewHolder(LayoutInflater.from(parent.context), parent).castNonNull()
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<BaseListModel, ViewBinding>, position: Int
    ) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<BaseListModel, ViewBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        holder.onBind(getItem(position), position)
    }

    override fun onViewRecycled(holder: BaseViewHolder<BaseListModel, ViewBinding>) {
        holder.onUnBind()
    }

    override fun getItemViewType(position: Int): Int {
        return listModelManager.getViewTypeAndRemember(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).modelId
    }

    fun getModelAtPosition(position: Int): BaseListModel? {
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

    abstract class BaseListModel protected constructor(val modelId: Long) {
        constructor(id: String) : this(Ids.hashString64Bit(id))

        abstract fun createViewHolder(
            inflater: LayoutInflater, container: ViewGroup
        ): BaseViewHolder<*, *>

        open fun areItemsTheSame(other: BaseListModel): Boolean {
            return this.modelId == other.modelId
        }

        open fun areContentsTheSame(other: BaseListModel): Boolean {
            return this == other
        }

        open fun getSpanSizeConfig(): BaseSpanSizeLookup.SpanSizeConfig =
            BaseSpanSizeLookup.SpanSizeConfig.Normal

        open fun getViewTypeConfig(): BaseListModelViewTypeConfig {
            return BaseListModelViewTypeConfig.Auto
        }

        fun attachTo(adapter: BaseListAdapter) {
            adapter.addModel(this)
        }

        fun attachTo(adapter: BaseListAdapter, constraint: () -> Boolean) {
            if (constraint()) {
                adapter.addModel(this)
            }
        }

        sealed class BaseListModelViewTypeConfig {
            data object Auto : BaseListModelViewTypeConfig()
            data class Manual(val viewType: Int) : BaseListModelViewTypeConfig()
        }
    }

    abstract class BaseViewHolder<T : BaseListModel, VB : ViewBinding>(val binding: VB) :
        RecyclerView.ViewHolder(binding.root) {

        protected val buildContext: Context = itemView.context

        abstract fun onBind(model: T, position: Int)
        abstract fun onUnBind()


    }

    class DiffCallback : DiffUtil.ItemCallback<BaseListModel>() {
        override fun areItemsTheSame(oldItem: BaseListModel, newItem: BaseListModel): Boolean {
            return oldItem.areItemsTheSame(newItem)
        }

        override fun areContentsTheSame(oldItem: BaseListModel, newItem: BaseListModel): Boolean {
            return oldItem.areContentsTheSame(newItem)
        }
    }
}
