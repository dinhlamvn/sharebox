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
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.optionmenu.OptionMenuBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.dpF
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.sharereceive.ShareReceiveActivity
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.WorkerUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@AndroidEntryPoint
@ActivityScoped
class HomeActivity : BaseViewModelActivity<HomeState, HomeViewModel, ActivityHomeBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    OptionMenuBottomSheetDialogFragment.OnOptionItemSelectedListener {

    val editBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { id ->
                    viewModel.reloadBoxDetail(id)
                }
            }
        }

    private val scrollListener = object : OnScrollListener() {
        var totalScrolledY: Int = 0
        var alpha: Float = 0f

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            totalScrolledY = totalScrolledY.plus(dy).coerceAtLeast(0)
            alpha = (totalScrolledY / 100.dpF()).coerceAtMost(1f)
            setupToolbarAction(recyclerView, alpha, totalScrolledY)
        }
    }

    private val viewBoxDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxDetail =
                    data.getParcelableExtraCompat<BoxDetail>(AppExtras.EXTRA_BOX_DETAIL)
                        ?: return@registerForActivityResult
                viewModel.refreshBoxDetail(boxDetail)
            }
        }

    private val chooseBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                val boxName = data.getStringExtra(AppExtras.EXTRA_BOX_NAME)
                    ?: return@registerForActivityResult
                onBoxSelected(boxId, boxName)
            }
        }

    private val moveShareToBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                viewModel.moveShareToBox(boxId)
            }
        }

    private val openShareTextResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.saveShareText(result.data?.getStringExtra(Intent.EXTRA_TEXT))
            }
        }

    private val changeShareNoteResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.saveShareNote(result.data?.getStringExtra(Intent.EXTRA_TEXT))
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
        val recyclerViewState = binding.recyclerView.layoutManager?.onSaveInstanceState()
        homeAdapter.requestBuildListModels {
            binding.recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
            setupToolbarAction(
                binding.recyclerView,
                scrollListener.alpha,
                scrollListener.totalScrolledY
            )
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

    private val archiveResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.doOnRefresh()
            }
        }

    private val archiveTextResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(Intent.EXTRA_TEXT)?.let(::onArchiveNote)
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
                archiveResultLauncher.launch(intent)
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

        binding.imageAdd.setImageDrawable(Icons.plusIcon(this))

        homeAdapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnRefresh()
        }

        binding.imageAdd.setOnClickListener {
            requestCreateBox()
        }

        viewModel.onChange(HomeState::asyncLoadSave, this) { asyncLoad ->
            binding.loading.isVisible = asyncLoad is BaseViewModel.AsyncLoad.Loading
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                viewModel.updateShare(asyncLoad.value)
            }
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
            requestArchiveNote()
        }

        binding.buttonArchiveWeb.setOnClickListener {
            requestArchiveWeb()
        }

        binding.buttonArchiveImages.setOnClickListener {
            requestArchiveImages()
        }

        binding.recyclerView.addOnScrollListener(scrollListener)
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
                1 -> onRequestChangeNote(share)
                2 -> onRequestMoveShare(share)
                3 -> WorkerUtils.enqueueDownloadShare(
                    this, share.shareData.cast<ShareData.ShareUrl>()?.url, share
                )

                4 -> onBookmark(shareId)
                5 -> copy(share.boxDetail?.boxId)
                6 -> moveToTrash(share)
            }
        }
    }

    private fun moveToTrash(share: ShareDetail) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirm_move_to_trash)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.moveShareToTrash(share.shareId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun onRequestMoveShare(share: ShareDetail) {
        viewModel.setCurrentShare(share)
        moveShareToBoxLauncher.launch(router.boxList(this, getString(R.string.move_share_to)))
    }

    private fun onRequestChangeNote(share: ShareDetail) {
        viewModel.setCurrentShare(share)
        changeShareNoteResultLauncher.launch(
            router.textInput(
                this,
                getString(R.string.note),
                share.shareNote
            )
        )
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                supportFragmentManager, shareId, collectionId
            )
        }
    }

    private fun requestCreateBox() {
        createBoxResultLauncher.launch(router.boxForm(this, null))
    }

    fun requestArchiveWeb() {
        archiveResultLauncher.launch(router.shareLink(this, null))
    }

    fun requestArchiveImages() {
        pickImagesResultLauncher.launch(router.pickImageIntent(true))
    }

    fun requestArchiveNote() {
        archiveTextResultLauncher.launch(router.textInput(this, null, null))
    }

    private fun onArchiveNote(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            component = ComponentName(packageName, ShareReceiveActivity::class.java.name)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        archiveResultLauncher.launch(intent)
    }

    private fun onBoxSelected(boxId: String, boxName: String) {
        val chooseBoxFor = getState(viewModel, HomeState::chooseBoxFor) ?: return
        viewModel.setChooseBoxFor(null)

        if (chooseBoxFor is HomeState.ChooseBoxFor.Detail) {
            openBox(boxId)
        } else if (chooseBoxFor is HomeState.ChooseBoxFor.Web) {
            router.moveToChromeCustomTab(
                this,
                chooseBoxFor.link,
                boxId,
                boxName,
                shareHelper.isSupportDownloadLink(chooseBoxFor.link)
            )
        }
    }

    fun requestViewAllBox() {
        viewModel.setChooseBoxFor(HomeState.ChooseBoxFor.Detail)
        showBoxList()
    }

    fun showMore(share: ShareDetail) {
        shareHelper.showMore(this, share, this)
    }

    fun openShare(share: ShareDetail) {
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> router.moveToChromeCustomTab(
                this,
                shareData.url,
                share.boxDetail?.boxId,
                share.boxDetail?.boxName,
                false
            )

            is ShareData.ShareText -> {
                viewModel.setCurrentShare(share)
                openShareTextResultLauncher.launch(router.textInput(this, null, shareData.text))
            }

            is ShareData.ShareImage -> shareHelper.viewShareImage(
                this, shareData.uri
            )

            is ShareData.ShareImages -> shareHelper.viewShareImages(
                this, shareData.uris
            )
        }
    }

    fun gotoLink(link: String) {
        viewModel.setChooseBoxFor(HomeState.ChooseBoxFor.Web(link))
        showBoxList(getString(R.string.choose_box_for_web))
    }

    private fun showBoxList(title: String? = null) {
        chooseBoxLauncher.launch(router.boxList(this, title))
    }

    fun openBox(boxId: String) {
        viewBoxDetailLauncher.launch(router.boxDetail(this, boxId))
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    @UiThread
    private fun setupToolbarAction(recyclerView: RecyclerView, alpha: Float, dy: Int) {
        recyclerView.findViewHolderForLayoutPosition(0)?.itemView?.apply {
            x = dy * -1f
            this.alpha = 1f - alpha
        }
        binding.containerAction.isVisible = alpha >= 0.2f
        binding.containerAction.alpha = alpha
        binding.textTitle.alpha = 1 - alpha
    }

    fun requestManageMembers(boxId: String) {
        if (userHelper.isSignedIn()) {
            startActivity(router.boxMembers(this, boxId))
        } else {
            showToast(R.string.require_sign_in_to_manage_member)
            startActivity(router.signIn(false))
        }
    }
}
