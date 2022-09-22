package com.dinhlam.sharesaver.base

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.utils.Ids

class BaseListAdapter<T : BaseListAdapter.BaseModelView> private constructor(
    private val viewHolderFactory: ViewHolderFactory<T, ViewBinding>
) : ListAdapter<T, BaseListAdapter.BaseViewHolder<T, ViewBinding>>(DiffCallback()) {

    init {
        setHasStableIds(true)
    }

    abstract class ModelViewsFactory : Runnable {

        private val buildModelThread = HandlerThread("build-model-thread")
        private val handler: Handler by lazy {
            buildModelThread.start()
            Handler(buildModelThread.looper)
        }

        private val mainHandler = Handler(Looper.getMainLooper())

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

        override fun run() {
            modelViews.clear()
            buildModelViews()
            adapter?.let {
                mainHandler.post {
                    it.submitList(modelViews.toList())
                }
            }
        }

        fun requestBuildModelViews(delayMills: Long = 0L) {
            handler.removeCallbacks(this)
            handler.postDelayed(this, delayMills)
        }
    }

    fun interface ViewHolderFactory<T : BaseModelView, VB : ViewBinding> {
        fun onCreateViewHolder(
            layoutRes: Int, itemView: View
        ): BaseViewHolder<T, VB>?
    }

    companion object {
        @JvmStatic
        fun <T : BaseModelView, VB : ViewBinding> createAdapter(
            factory: ViewHolderFactory<T, VB>
        ): BaseListAdapter<BaseModelView> {
            return BaseListAdapter(factory.cast()!!)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BaseViewHolder<T, ViewBinding> {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewType, parent, false)
        return viewHolderFactory.onCreateViewHolder(viewType, view)
            ?: throw IllegalStateException("VewHolder of view type $viewType undefined")
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
        return getItem(position).layoutRes
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

        abstract val layoutRes: Int

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