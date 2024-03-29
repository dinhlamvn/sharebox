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
import com.dinhlam.sharebox.extensions.heightPercentage
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.LocalStorageHelper
import com.dinhlam.sharebox.listmodel.ImageListModel
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ViewImagesDialogFragment : BaseDialogFragment<DialogViewImagesBinding>() {

    @Inject
    lateinit var localStorageHelper: LocalStorageHelper

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogViewImagesBinding {
        return DialogViewImagesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments ?: return dismiss()
        val uris = args.getParcelableArrayListExtraCompat<Uri>(AppExtras.EXTRA_IMAGE_URIS)
            ?: return dismiss()

        val adapter = BaseListAdapter.createAdapter {
            uris.forEach { uri ->
                ImageListModel(
                    uri, height = heightPercentage(80)
                ).attachTo(this)
            }
        }

        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager

        val spanHelper = PagerSnapHelper()
        spanHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.adapter = adapter
        adapter.requestBuildModelViews()
        updatePageNumber(1, uris.size)

        binding.recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePageNumber(layoutManager.findFirstVisibleItemPosition() + 1, uris.size)
                }
            }
        })

        binding.imageSaveToGallery.setImageDrawable(Icons.saveIcon(requireContext()))
        binding.imageSaveToGallery.setOnClickListener {
            val currentPos = layoutManager.findFirstVisibleItemPosition()
            val uri = uris.getOrNull(currentPos) ?: return@setOnClickListener
            binding.viewLoading.show()
            fragmentScope.launch(Dispatchers.IO) {
                localStorageHelper.saveImageToGallery(uri)
                withContext(Dispatchers.Main) {
                    binding.viewLoading.hide()
                    showToast(R.string.success_save_image_to_gallery)
                }
            }
        }
    }

    private fun updatePageNumber(position: Int, size: Int) {
        binding.textNumber.text = getString(R.string.page_number, position, size)
    }

    override fun getTheme(): Int {
        return R.style.AppTheme_AlertDialog
    }
}