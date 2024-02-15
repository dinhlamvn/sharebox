package com.dinhlam.sharebox.ui.home

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.singlechoice.SingleChoiceBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.services.RealtimeDatabaseService
import com.dinhlam.sharebox.ui.sharereceive.ShareReceiveActivity
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.LiveEventUtils
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@AndroidEntryPoint
@ActivityScoped
class HomeActivity : BaseViewModelActivity<HomeState, HomeViewModel, ActivityHomeBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    SingleChoiceBottomSheetDialogFragment.OnOptionItemSelectedListener {

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var shareHelper: ShareHelper

    override fun onStateChanged(state: HomeState) {
        homeAdapter.requestBuildModelViews()
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(this, LinearLayoutManager.VERTICAL, blockShouldLoadMore = {
            return@LoadMoreLinearLayoutManager getState(viewModel) { state -> state.canLoadMore && !state.isLoadingMore }
        }) {
            viewModel.loadMores()
        }
    }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { _ ->
                    viewModel.doOnRefresh()
                }
            }
        }

    private val realtimeDatabaseServiceIntent by lazy(LazyThreadSafetyMode.NONE) {
        Intent(this, RealtimeDatabaseService::class.java)
    }

    private val shareResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                LiveEventUtils.eventScrollToTopGeneral.postValue(true)
            }
        }

    private val shareLinkResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val link =
                    data.getStringExtra(AppExtras.EXTRA_URL) ?: return@registerForActivityResult
                val boxId = data.getStringExtra(AppExtras.EXTRA_BOX_ID)
                val boxName = data.getStringExtra(AppExtras.EXTRA_BOX_NAME)
                router.moveToChromeCustomTab(
                    this,
                    link,
                    boxId,
                    boxName,
                    shareHelper.isSupportDownloadLink(link)
                )
            }
        }

    private val shareTextResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(Intent.EXTRA_TEXT)?.let(::onShareText)
            }
        }

    private val pickImagesResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData ?: return@registerForActivityResult
                val pickCount =
                    clipData.itemCount.takeIfGreaterThanZero() ?: return@registerForActivityResult
                val intent = if (pickCount == 1) {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        component =
                            ComponentName(packageName, ShareReceiveActivity::class.java.name)
                        putExtra(Intent.EXTRA_STREAM, clipData.getItemAt(0).uri)
                    }
                } else {
                    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "image/*"
                        component =
                            ComponentName(packageName, ShareReceiveActivity::class.java.name)
                        val list = arrayListOf<Uri>()
                        for (i in 0 until pickCount) {
                            list.add(clipData.getItemAt(i).uri)
                        }
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, list)
                    }
                }
                shareResultLauncher.launch(intent)
            }
        }

    @Inject
    lateinit var homeAdapter: HomeAdapter

    override fun onCreateViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressHandler {
            if (binding.recyclerView.computeVerticalScrollOffset() > 0) {
                binding.recyclerView.smoothScrollToPosition(0)
            } else {
                finish()
            }
        }

        binding.imageProfile.setImageDrawable(Icons.userIcon(this))
        binding.imageProfile.setOnClickListener {
            startActivity(router.profile(this))
        }

        ContextCompat.startForegroundService(this, realtimeDatabaseServiceIntent)

        binding.recyclerView.layoutManager = layoutManager
        homeAdapter.attachTo(binding.recyclerView, this)

        viewModel.consume(this, HomeState::isLoadingMore) { isLoadMore ->
            layoutManager.hadTriggerLoadMore = isLoadMore
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnRefresh()
        }

        binding.buttonCreateBox.setOnClickListener {
            requestCreateBox()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(realtimeDatabaseServiceIntent)
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.bookmark(shareId, bookmarkCollectionId)
    }

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        getState(viewModel) { state ->
            val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return@getState
            val share =
                state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState

            when (position) {
                0 -> shareHelper.shareToOther(share)
                1 -> onBookmark(shareId)
                2 -> WorkerUtils.enqueueDownloadShare(
                    this, share.shareData.cast<ShareData.ShareUrl>()?.url
                )

                3 -> onOpen(shareId)
            }
        }
    }

    private fun onOpen(shareId: String) = getState(viewModel) { state ->
        val share = state.shares.firstOrNull { shareDetail -> shareDetail.shareId == shareId }
            ?: return@getState
        openShare(share)
    }

    fun openShare(share: ShareDetail) {
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> router.moveToBrowser(shareData.url)
            is ShareData.ShareText -> {
                shareHelper.openTextViewerDialog(this, shareData.text)
            }

            is ShareData.ShareImage -> shareHelper.viewShareImage(
                this, share.shareId, shareData.uri
            )

            is ShareData.ShareImages -> shareHelper.viewShareImages(
                this, share.shareId, shareData.uris
            )
        }
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                supportFragmentManager, shareId, collectionId
            )
        }
    }

    fun requestCreateBox() {
        createBoxResultLauncher.launch(router.boxIntent(this))
    }

    fun requestShareLink() {
        shareLinkResultLauncher.launch(router.shareLink(this))
    }

    fun requestShareImages() {
        pickImagesResultLauncher.launch(router.pickImageIntent(true))
    }

    fun requestShareText() {
        shareTextResultLauncher.launch(router.shareText(this))
    }

    private fun onShareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            component = ComponentName(packageName, ShareReceiveActivity::class.java.name)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(intent)
    }
}
