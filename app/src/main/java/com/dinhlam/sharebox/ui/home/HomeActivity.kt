package com.dinhlam.sharebox.ui.home

import android.annotation.TargetApi
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.dialog.optionmenu.OptionMenuBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.dpF
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.sharereceive.ShareReceiveActivity
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@AndroidEntryPoint
@ActivityScoped
class HomeActivity : BaseViewModelActivity<HomeState, HomeViewModel, ActivityHomeBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    OptionMenuBottomSheetDialogFragment.OnOptionItemSelectedListener,
    BoxSelectionDialogFragment.OnBoxSelectedListener {

    private val openShareTextResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.doOnRefresh()
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showAlertDialog()
            }
        }

    override val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var shareHelper: ShareHelper


    @Inject
    lateinit var userHelper: UserHelper

    override fun onStateChanged(state: HomeState) {
        homeAdapter.requestBuildListModels()
    }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { _ ->
                    viewModel.doOnRefresh()
                }
            }
        }

    private val shareResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.doOnRefresh()
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

        setupActionBarAction()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                showAlertDialog()
            }
        }

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

        homeAdapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnRefresh()
        }

        binding.buttonCreateBox.setOnClickListener {
            requestCreateBox()
        }
    }

    private fun setupActionBarAction() {
        binding.containerAction.isVisible = false
        binding.buttonArchiveText.setIcon(Icons.noteIcon(this) {
            copy(colorRes = android.R.color.white)
        })
        binding.buttonArchiveWeb.setIcon(Icons.webIcon(this) {
            copy(colorRes = android.R.color.white)
        })
        binding.buttonArchiveImages.setIcon(Icons.imageIcon(this) {
            copy(colorRes = android.R.color.white)
        })

        binding.buttonArchiveText.setOnClickListener {
            requestShareText()
        }

        binding.buttonArchiveWeb.setOnClickListener {
            requestShareWeb()
        }

        binding.buttonArchiveImages.setOnClickListener {
            requestShareImages()
        }

        binding.recyclerView.addOnScrollListener(object : OnScrollListener() {
            private var totalScrolledY: Int = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalScrolledY = totalScrolledY.plus(dy).coerceAtLeast(0).coerceAtMost(100.dp())
                val alpha = (totalScrolledY / 100.dpF()).coerceAtMost(1f)
                binding.containerAction.isVisible = alpha >= 0.2f
                binding.containerAction.alpha = alpha
                binding.textTitle.alpha = 1 - alpha
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (userHelper.getCurrentUserId() != getState(viewModel, HomeState::currentUserId)) {
            viewModel.doOnRefresh()
        }
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private fun showAlertDialog() {
        AlertDialog.Builder(this).setTitle(R.string.alert_notice)
            .setMessage(R.string.alert_request_post_notification_permission_message)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }.setNegativeButton(R.string.alert_no_thanks) { _, _ ->
                showToast(R.string.permission_denied)
            }.create().show()

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
                1 -> WorkerUtils.enqueueDownloadShare(
                    this, share.shareData.cast<ShareData.ShareUrl>()?.url, share
                )

                2 -> onBookmark(shareId)
                3 -> copy(share.boxDetail?.boxId)
            }
        }
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                supportFragmentManager, shareId, collectionId
            )
        }
    }

    private fun requestCreateBox() {
        createBoxResultLauncher.launch(router.boxIntent(this))
    }

    fun requestShareWeb() {
        shareResultLauncher.launch(router.shareLink(this))
    }

    fun requestShareImages() {
        pickImagesResultLauncher.launch(router.pickImageIntent(true))
    }

    fun requestShareText() {
        shareTextResultLauncher.launch(router.shareText(this, null))
    }

    private fun onShareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            component = ComponentName(packageName, ShareReceiveActivity::class.java.name)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        shareResultLauncher.launch(intent)
    }

    override fun onBoxSelected(boxId: String) {
        startActivity(router.boxDetail(this, boxId))
    }

    fun requestViewAllBox() {
        shareHelper.showBoxSelectionDialog(supportFragmentManager)
    }

    fun showMore(share: ShareDetail) {
        shareHelper.showMore(this, share)
    }

    fun openShare(share: ShareDetail) {
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> router.moveToBrowser(shareData.url)
            is ShareData.ShareText -> {
                openShareTextResultLauncher.launch(router.shareText(this, share.shareId))
            }

            is ShareData.ShareImage -> shareHelper.viewShareImage(
                this, shareData.uri
            )

            is ShareData.ShareImages -> shareHelper.viewShareImages(
                this, shareData.uris
            )
        }
    }
}
