package com.dinhlam.sharebox.ui.home

import android.annotation.TargetApi
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogLayoutInputBinding
import com.dinhlam.sharebox.databinding.FragmentHomeBinding
import com.dinhlam.sharebox.dialog.optionmenu.BottomSheetOptionsMenuDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.copy
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.openShare
import com.dinhlam.sharebox.extensions.packageName
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.model.BoxDetail
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.main.MainActivity
import com.dinhlam.sharebox.ui.sharereceive.ShareReceiveActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment :
    BaseViewModelFragment<HomeState, HomeViewModel, FragmentHomeBinding>() {

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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showAlertDialog()
            }
        }

    private val passcodeConfirmResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                onEditBox(boxId)
            } else {
                showToast(R.string.error_require_passcode)
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
                startActivity(intent)
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
                startActivity(intent)
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

        binding.iconInfo.setOnClickListener {
            startActivity(router.guideline(requireContext()))
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showAlertDialog() {
        AlertDialog.Builder(requireContext()).setTitle(R.string.alert_notice)
            .setMessage(R.string.alert_request_post_notification_permission_message)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }.setNegativeButton(R.string.alert_no_thanks) { _, _ ->
                showToast(R.string.permission_denied)
            }.create().show()

    }

    private fun onArchiveNote(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            component = ComponentName(packageName, ShareReceiveActivity::class.java.name)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(intent)
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
                boxName
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

    fun requestArchiveNote(view: View) {
        val popupMenu = PopupMenu(requireActivity(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_archive_note, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.text -> archiveTextResultLauncher.launch(
                    router.textInput(
                        requireContext(),
                        null,
                        null,
                        false
                    )
                )

                R.id.checklist -> startActivity(router.checkList(requireContext(), null))
            }
            true
        }
        popupMenu.show()
    }

    fun requestArchiveWeb(view: View) {
        val popupMenu = PopupMenu(requireActivity(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_archive_web, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.link -> showDialogInputLink()

                R.id.browser -> startActivity(router.shareLink(requireContext(), null))
            }
            true
        }
        popupMenu.show()

    }

    private fun showDialogInputLink() {
        val binding = DialogLayoutInputBinding.inflate(LayoutInflater.from(requireContext()))
        binding.dialogTextInputEdit.inputType = InputType.TYPE_CLASS_TEXT
        binding.dialogTextInputEdit.hint = getString(R.string.share_link_hint)
        MaterialAlertDialogBuilder(requireContext())
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                val link = binding.dialogTextInputEdit.text?.trimmedString().orEmpty()
                if (link.isWebLink()) {
                    onArchiveNote(link)
                } else {
                    showToast(R.string.require_input_correct_weblink)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setView(binding.root)
            .show()
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
        shareHelper.showMore(requireActivity(), shareDetail, viewModel::refresh)
    }

    fun openBox(boxId: String) {
        viewBoxDetailLauncher.launch(router.boxDetail(requireContext(), boxId))
    }

    private fun editBox(boxDetail: BoxDetail) {
        if (boxDetail.isHasPasscode) {
            passcodeConfirmResultLauncher.launch(
                router.passcodeIntent(
                    requireContext(),
                    boxDetail.passcode,
                    bundleOf(AppExtras.EXTRA_BOX_ID to boxDetail.boxId),
                    getString(
                        R.string.dialog_bookmark_collection_picker_verify_passcode,
                        boxDetail.boxName
                    )
                )
            )
        } else {
            onEditBox(boxDetail.boxId)
        }
    }

    private fun onEditBox(boxId: String) {
        startActivity(router.boxForm(requireContext(), boxId))
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
            BottomSheetOptionsMenuDialogFragment.SingleChoiceItem(
                "f044",
                getString(R.string.title_edit_box)
            ),
            BottomSheetOptionsMenuDialogFragment.SingleChoiceItem(
                "f0c0",
                getString(R.string.members)
            ),
            BottomSheetOptionsMenuDialogFragment.SingleChoiceItem(
                "f0c5",
                getString(R.string.copy_id)
            )
        )
        BottomSheetOptionsMenuDialogFragment.show(
            childFragmentManager,
            items
        ) { position, _, _ ->
            when (position) {
                0 -> editBox(boxDetail)
                1 -> requestManageMembers(boxDetail.boxId)
                2 -> context?.copy(boxDetail.boxId)
            }
        }
    }

    fun moveToDiscover(tab: Int) {
        activity?.cast<MainActivity>()?.moveToDiscover(tab)
    }

    fun requestCreateBox() {
        startActivity(router.boxForm(requireContext(), null))
    }

    fun openShare(shareDetail: ShareDetail) {
        context?.openShare(childFragmentManager, shareDetail, router, shareHelper)
    }
}
