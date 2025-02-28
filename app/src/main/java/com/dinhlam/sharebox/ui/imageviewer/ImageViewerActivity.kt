package com.dinhlam.sharebox.ui.imageviewer

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityImageViewerBinding
import com.dinhlam.sharebox.extensions.getParcelableArrayListExtraCompat
import com.dinhlam.sharebox.extensions.heightPercentage
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.listmodel.ImageListModel
import com.dinhlam.sharebox.storage.LocalStorageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ImageViewerActivity : BaseActivity<ActivityImageViewerBinding>() {

    @Inject
    lateinit var localStorageManager: LocalStorageManager

    val uris: List<Uri> by lazy {
        intent.getParcelableArrayListExtraCompat<Uri>(AppExtras.EXTRA_IMAGE_URIS)
            ?: emptyList()
    }

    val adapter = BaseListAdapter.create {
        uris.forEach { uri ->
            ImageListModel(
                uri, height = heightPercentage(80)
            ).attachTo(this)
        }
    }

    override fun onCreateViewBinding(): ActivityImageViewerBinding {
        return ActivityImageViewerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager

        val spanHelper = PagerSnapHelper()
        spanHelper.attachToRecyclerView(binding.recyclerView)
        adapter.attachTo(binding.recyclerView, this)
        updatePageNumber(1, uris.size)

        binding.recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePageNumber(layoutManager.findFirstVisibleItemPosition() + 1, uris.size)
                }
            }
        })

        binding.imageSaveToGallery.setOnClickListener {
            val currentPos = layoutManager.findFirstVisibleItemPosition()
            val uri = uris.getOrNull(currentPos) ?: return@setOnClickListener
            binding.viewLoading.show()
            activityScope.launch(Dispatchers.IO) {
                localStorageManager.saveImageToGallery(uri)
                withContext(Dispatchers.Main) {
                    binding.viewLoading.hide()
                    showToast(R.string.success_save_image_to_gallery)
                }
            }
        }
    }

    private fun updatePageNumber(position: Int, size: Int) {
        supportActionBar?.title = getString(R.string.page_number, position, size)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}