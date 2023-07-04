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
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogViewImagesBinding
import com.dinhlam.sharebox.extensions.getParcelableArrayListExtraCompat
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.modelview.ImageModelView

class ViewImagesDialogFragment : BaseDialogFragment<DialogViewImagesBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogViewImagesBinding {
        return DialogViewImagesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uris =
            arguments?.getParcelableArrayListExtraCompat<Uri>(AppExtras.EXTRA_IMAGE_URIS)
                ?: return dismiss()

        val adapter = BaseListAdapter.createAdapter {
            addAll(uris.map { uri -> ImageModelView(uri) })
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

    override fun getTheme(): Int {
        return R.style.AppTheme_AlertDialog
    }
}