package com.dinhlam.sharebox.dialog.viewimages

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.DialogViewImagesBinding
import com.dinhlam.sharebox.databinding.ModelViewImageBinding
import com.dinhlam.sharebox.extensions.getParcelableArrayListExtraCompat
import com.dinhlam.sharebox.modelview.ImageModelView

class ViewImagesDialogFragment : BaseDialogFragment<DialogViewImagesBinding>() {

    companion object {
        const val EXTRA_LIST_URI = "extra-uris"
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogViewImagesBinding {
        return DialogViewImagesBinding.inflate(inflater, container, false)
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        val uris =
            arguments?.getParcelableArrayListExtraCompat<Uri>(EXTRA_LIST_URI) ?: return dismiss()

        val adapter = BaseListAdapter.createAdapter({
            addAll(uris.map { ImageModelView(it) })
        }) {
            withViewType(R.layout.model_view_image) {
                ImageModelView.ImageViewHolder(ModelViewImageBinding.bind(this))
            }
        }

        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        viewBinding.recyclerView.layoutManager = layoutManager

        val spanHelper = PagerSnapHelper()
        spanHelper.attachToRecyclerView(viewBinding.recyclerView)
        viewBinding.recyclerView.adapter = adapter
        adapter.requestBuildModelViews()
        updatePageNumber(1, uris.size)


        viewBinding.recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePageNumber(layoutManager.findFirstVisibleItemPosition() + 1, uris.size)
                }
            }
        })
    }

    private fun updatePageNumber(position: Int, size: Int) {
        viewBinding.textNumber.text = getString(R.string.page_number, position, size)
    }
}