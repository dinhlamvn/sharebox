package com.dinhlam.sharebox.ui.home

import android.annotation.TargetApi
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.FragmentHomeBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.download.DownloadFileDialogFragment
import com.dinhlam.sharebox.dialog.optionmenu.OptionMenuBottomSheetDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.packageName
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.main.MainActivity
import com.dinhlam.sharebox.ui.sharereceive.ShareReceiveActivity
import com.dinhlam.sharebox.utils.WorkerUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment :
    BaseViewModelFragment<HomeState, HomeViewModel, FragmentHomeBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener,
    OptionMenuBottomSheetDialogFragment.OnOptionItemSelectedListener {

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.refresh()
            }
        }

    private val editBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { id ->
                    viewModel.reloadBoxDetail(id)
                }
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
        homeAdapter.requestBuildListModels()
    }

    private val archiveResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.refresh()
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

    private val pickFileResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "*/*"
                    component =
                        ComponentName(packageName, ShareReceiveActivity::class.java.name)
                    putExtra(Intent.EXTRA_STREAM, uri)
                }
                archiveResultLauncher.launch(intent)
            }
        }

    @Inject
    lateinit var homeAdapter: HomeAdapter

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                showAlertDialog()
            }
        }

        homeAdapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refresh()
        }

        onChange(HomeState::asyncLoadSave) { asyncLoad ->
            binding.loading.isVisible = asyncLoad is BaseViewModel.AsyncLoad.Loading
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                viewModel.refresh()
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private fun showAlertDialog() {
        AlertDialog.Builder(requireContext()).setTitle(R.string.alert_notice)
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
                    requireContext(), share.shareData.cast<ShareData.ShareUrl>()?.url, share
                )

                4 -> onBookmark(shareId)
                5 -> context?.copy(share.boxDetail?.boxId)
                6 -> moveToTrash(share)
            }
        }
    }

    private fun moveToTrash(share: ShareDetail) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.confirm_move_to_trash)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.moveShareToTrash(share.shareId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun onRequestMoveShare(share: ShareDetail) {
        viewModel.setCurrentShare(share)
        moveShareToBoxLauncher.launch(
            router.boxList(
                requireContext(),
                getString(R.string.move_share_to)
            )
        )
    }

    private fun onRequestChangeNote(share: ShareDetail) {
        viewModel.setCurrentShare(share)
        changeShareNoteResultLauncher.launch(
            router.textInput(
                requireContext(),
                getString(R.string.note),
                share.shareNote,
                true
            )
        )
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPickerDialog(
                childFragmentManager, shareId, collectionId
            )
        }
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
                requireContext(),
                chooseBoxFor.link,
                boxId,
                boxName,
                shareHelper.isSupportDownloadLink(chooseBoxFor.link)
            )
        }
    }

    private fun showBoxList(title: String? = null) {
        chooseBoxLauncher.launch(router.boxList(requireContext(), title))
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    fun requestArchiveNote() {
        archiveTextResultLauncher.launch(router.textInput(requireContext(), null, null, false))
    }

    fun requestArchiveWeb() {
        archiveResultLauncher.launch(router.shareLink(requireContext(), null))
    }

    fun requestArchiveImages() {
        pickImagesResultLauncher.launch(router.pickImageIntent(true))
    }

    fun requestArchiveFile() {
        pickFileResultLauncher.launch(router.pickFile(requireContext()))
    }

    fun requestViewAllBox() {
        viewModel.setChooseBoxFor(HomeState.ChooseBoxFor.Detail)
        showBoxList()
    }

    fun showMore(shareDetail: ShareDetail) {
        shareHelper.showMore(requireActivity(), shareDetail, this@HomeFragment)
    }

    fun openShare(shareDetail: ShareDetail) {
        when (val shareData = shareDetail.shareData) {
            is ShareData.ShareUrl -> router.moveToChromeCustomTab(
                requireContext(),
                shareData.url,
                shareDetail.boxDetail?.boxId,
                shareDetail.boxDetail?.boxName,
                false
            )

            is ShareData.ShareText -> {
                viewModel.setCurrentShare(shareDetail)
                openShareTextResultLauncher.launch(
                    router.textInput(
                        requireContext(),
                        null,
                        shareData.text,
                        true
                    )
                )
            }

            is ShareData.ShareImage -> shareHelper.viewShareImage(
                requireContext(), shareData.uri
            )

            is ShareData.ShareImages -> shareHelper.viewShareImages(
                requireContext(), shareData.uris
            )

            is ShareData.ShareFile -> {
                val downloadUrl = shareData.uri.toString()
                DownloadFileDialogFragment.showDialog(
                    childFragmentManager,
                    downloadUrl,
                    shareData.fileName,
                    shareData.mimeType
                )
            }
        }
    }

    fun openBox(boxId: String) {
        viewBoxDetailLauncher.launch(router.boxDetail(requireContext(), boxId, false))
    }

    private fun editBox(boxId: String) {
        editBoxResultLauncher.launch(
            router.boxForm(
                requireContext(),
                boxId
            )
        )
    }

    private fun requestManageMembers(boxId: String) {
        if (userHelper.isSignedIn()) {
            startActivity(router.boxMembers(requireContext(), boxId))
        } else {
            showToast(R.string.require_sign_in_to_manage_member)
            startActivity(router.signIn(false))
        }
    }

    fun showBoxOption(boxDetail: BoxDetail) {
        val items = arrayOf(
            OptionMenuBottomSheetDialogFragment.SingleChoiceItem(
                FontAwesome.Icon.faw_edit.name,
                getString(R.string.title_edit_box)
            ),
            OptionMenuBottomSheetDialogFragment.SingleChoiceItem(
                FontAwesome.Icon.faw_users.name,
                getString(R.string.members)
            ),
            OptionMenuBottomSheetDialogFragment.SingleChoiceItem(
                FontAwesome.Icon.faw_copy.name,
                getString(R.string.copy_id)
            )
        )
        OptionMenuBottomSheetDialogFragment.show(
            childFragmentManager,
            items
        ) { position, _, _ ->
            when (position) {
                0 -> editBox(boxDetail.boxId)

                1 -> requestManageMembers(boxDetail.boxId)

                2 -> context?.copy(boxDetail.boxId)
            }
        }
    }

    fun moveToDiscover(tab: Int) {
        activity?.cast<MainActivity>()?.moveToDiscover(tab)
    }

    fun requestCreateBox() {
        createBoxResultLauncher.launch(router.boxForm(requireContext(), null))
    }
}
