package com.dinhlam.sharebox.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetDialogFragment
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.dialog.folder.confirmpassword.FolderConfirmPasswordDialogFragment
import com.dinhlam.sharebox.dialog.folder.creator.FolderCreatorDialogFragment
import com.dinhlam.sharebox.dialog.folder.detail.FolderDetailDialogFragment
import com.dinhlam.sharebox.dialog.folder.rename.RenameFolderDialogFragment
import com.dinhlam.sharebox.dialog.folder.resetpassword.ResetPasswordFolderDialogFragment
import com.dinhlam.sharebox.dialog.guideline.GuidelineDialogFragment
import com.dinhlam.sharebox.dialog.singlechoose.SingleChoiceDialogFragment
import com.dinhlam.sharebox.dialog.tag.ChoiceTagDialogFragment
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.extensions.*
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.model.ShareType
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.modelview.FolderListModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SingleTextModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.ui.home.modelview.recently.ShareRecentlyImageModelView
import com.dinhlam.sharebox.ui.home.modelview.recently.ShareRecentlyTextModelView
import com.dinhlam.sharebox.ui.home.modelview.recently.ShareRecentlyWebLinkModelView
import com.dinhlam.sharebox.ui.share.ShareState
import com.dinhlam.sharebox.utils.ExtraUtils
import com.dinhlam.sharebox.utils.FolderUtils
import com.dinhlam.sharebox.utils.TagUtil
import com.dinhlam.sharebox.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseViewModelActivity<HomeState, HomeViewModel, ActivityHomeBinding>(),
    FolderCreatorDialogFragment.OnFolderCreatorCallback,
    FolderConfirmPasswordDialogFragment.OnConfirmPasswordCallback,
    RenameFolderDialogFragment.OnConfirmRenameCallback,
    ChoiceTagDialogFragment.OnTagSelectedListener {

    private val settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val sortType = result.data?.getSerializableExtraCompat<SortType>("sort-type")
                    ?: return@registerForActivityResult
                viewModel.setSortType(sortType)
            }
        }

    companion object {
        private const val POPUP_ITEM_SPACING = 60
    }

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var shareHelper: ShareHelper

    override val viewModel: HomeViewModel by viewModels()

    private val shareListAdapter by lazy {
        BaseListAdapter.createAdapter(HomeShareListModelViewsBuilder(this, viewModel, gson)) {
            val percentWidth = screenWidth().times(0.8f).toInt()

            withViewType(R.layout.model_view_loading) {
                LoadingViewHolder(this)
            }

            withViewType(R.layout.model_view_share_recently_text) {
                ShareRecentlyTextModelView.ShareRecentlyTextViewHolder(this, { textContent ->
                    val dialog = TextViewerDialogFragment()
                    dialog.arguments = Bundle().apply {
                        putString(Intent.EXTRA_TEXT, textContent)
                    }
                    dialog.show(supportFragmentManager, "TextViewerDialogFragment")
                }, ::showDialogShareToOther).apply {
                    updateLayoutParams {
                        width = percentWidth
                    }
                }
            }

            withViewType(R.layout.model_view_share_recently_web_link) {
                ShareRecentlyWebLinkModelView.ShareRecentlyWebLinkWebHolder(
                    this, ::openRecentlyShareWebLink, ::showDialogShareToOther
                ).apply {
                    updateLayoutParams {
                        width = percentWidth
                    }
                }
            }

            withViewType(R.layout.model_view_share_recently_image) {
                ShareRecentlyImageModelView.ShareRecentlyImageViewHolder(
                    this, ::showDialogShareToOther, ::viewImage
                ).apply {
                    updateLayoutParams {
                        width = percentWidth
                    }
                }
            }
        }
    }

    private val homeAdapter = BaseListAdapter.createAdapter({
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView)
                return@getState
            }
            val folders = state.folders

            if (folders.isEmpty()) {
                add(SingleTextModelView(getString(R.string.home_folder_list_empty)))
            } else {
                folders.forEach { folder ->
                    add(
                        FolderListModelView(
                            "folder_${folder.id}",
                            folder.name,
                            folder.desc,
                            folder.updatedAt,
                            folder.isHasPassword(),
                            TagUtil.getTag(folder.tag)
                        )
                    )
                }
            }
        }
    }) {
        withViewType(R.layout.model_view_loading) {
            LoadingViewHolder(this)
        }

        withViewType(R.layout.model_view_single_text) {
            SingleTextModelView.SingleTextViewHolder(this)
        }

        withViewType(R.layout.model_view_folder_list) {
            FolderListModelView.FolderListViewHolder(
                this, viewModel::onFolderClick, ::onFolderOptionClick
            )
        }
    }

    override fun onCreateViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setSortType(appSharePref.getSortType())

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        viewBinding.recyclerView.adapter = homeAdapter

        viewBinding.recyclerView.addOnScrollListener(object : OnScrollListener() {
            private var scrolled = 0
            private val rangeToHideGuideButton = 100

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                scrolled += dy

                if (scrolled > rangeToHideGuideButton) {
                    viewBinding.buttonCreateFolder.isVisible = false
                    scrolled = 0
                } else if (scrolled < -rangeToHideGuideButton) {
                    viewBinding.buttonCreateFolder.isVisible = true
                    scrolled = 0
                }
            }
        })

        viewBinding.recyclerViewShareRecently.adapter = shareListAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadFolders()
            viewModel.loadShareListRecently()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.consume(this, HomeState::toastRes) { toastRes ->
            if (toastRes != 0) {
                showToast(getString(toastRes))
                viewModel.clearToast()
            }
        }

        viewModel.consume(this, HomeState::folderActionConfirmation, true, ::handleFolderAction)

        viewModel.consume(this, HomeState::folderToOpen) { folderToOpen ->
            folderToOpen?.let {
                viewModel.clearOpenFolder()
                openFolder(it)
            }
        }

        viewModel.consume(this, HomeState::tag) {
            invalidateOptionsMenu()
        }

        viewModel.consume(this, HomeState::shareList, true) { shares ->
            viewBinding.recyclerViewShareRecently.isVisible = shares.isNotEmpty()
        }

        viewBinding.buttonCreateFolder.setOnClickListener {
            showDialogCreateFolder()
        }

        if (appSharePref.isShowGuideLine()) {
            openGuideLineDialog()
            appSharePref.turnOffShowGuideline()
        }
    }

    private fun openGuideLineDialog() {
        BaseDialogFragment.showDialog(GuidelineDialogFragment::class, supportFragmentManager)
    }

    private fun openFolder(folder: Folder) {
        startActivity(appRouter.shareList(folder.id))
    }

    override fun onStateChanged(state: HomeState) {
        homeAdapter.requestBuildModelViews()
        shareListAdapter.requestBuildModelViews()
        viewBinding.frameProgress.frameContainer.isVisible = state.showProgress
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.item_clear_tag)?.isVisible = getState(viewModel) { it.tag != null }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu.cast<MenuBuilder>()?.setOptionalIconsVisible(true)
        return menuInflater.inflate(R.menu.home_menu, menu).let { true }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_setting) {
            settingLauncher.launch(appRouter.setting())
            return true
        }

        if (viewModel.isHandleByTagSelected(item.itemId)) {
            return true
        }

        if (item.itemId == R.id.item_clear_tag) {
            viewModel.clearSelectedTag()
            return true
        }

        if (item.itemId == R.id.item_about) {
            showAbout()
            return true
        }

        if (item.itemId == R.id.item_guideline) {
            openGuideLineDialog()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDialogCreateFolder() {
        val dialog = FolderCreatorDialogFragment()
        dialog.show(supportFragmentManager, "DialogCreateFolder")
    }

    override fun onFolderCreated(folderId: String) {
        viewModel.loadFolders()
    }

    private fun onFolderOptionClick(clickedView: View, position: Int) {
        val folder = getState(viewModel) { state -> state.folders.getOrNull(position) } ?: return
        val items = mutableMapOf<String, () -> Unit>()
        val icons = mutableListOf<Int>()

        if (!FolderUtils.isProtectedFolder(folder.id)) {
            items[getString(R.string.delete)] = {
                viewModel.processFolderForDelete(folder)
            }
            icons.add(R.drawable.ic_delete)

            items[getString(R.string.rename)] = {
                viewModel.processFolderForRename(folder)
            }
            icons.add(R.drawable.ic_rename)
        }

        if (folder.isHasPassword()) {
            items[getString(R.string.reset_password)] = {
                viewModel.processFolderForResetPassword(folder)
            }
            icons.add(R.drawable.ic_reset_password)
        }

        items[getString(R.string.tag)] = {
            viewModel.processFolderForTag(folder)
        }
        icons.add(R.drawable.ic_tag)

        items[getString(R.string.detail)] = {
            viewModel.processFolderForDetail(folder)
        }
        icons.add(R.drawable.ic_detail)

        BaseBottomSheetDialogFragment.showDialog(
            SingleChoiceDialogFragment::class, supportFragmentManager
        ) {
            arguments = Bundle().apply {
                putStringArray(
                    SingleChoiceDialogFragment.EXTRA_ITEM, items.keys.toTypedArray()
                )
                putIntArray(SingleChoiceDialogFragment.EXTRA_ICON, icons.toIntArray())
            }
            listener = SingleChoiceDialogFragment.OnItemSelectedListener { position ->
                val key = items.keys.toList()[position]
                items[key]?.invoke()
            }
        }
    }

    private fun handleFolderAction(confirmation: HomeState.FolderActionConfirmation?) {
        val nonNull = confirmation ?: return
        return when (nonNull.folderActionType) {
            HomeState.FolderActionConfirmation.FolderActionType.DELETE -> maybeShowConfirmPasswordToDeleteFolder(
                nonNull
            )
            HomeState.FolderActionConfirmation.FolderActionType.OPEN -> maybeShowConfirmPasswordToOpenFolder(
                nonNull
            )
            HomeState.FolderActionConfirmation.FolderActionType.RENAME -> maybeShowConfirmPasswordToRenameFolder(
                nonNull
            )
            HomeState.FolderActionConfirmation.FolderActionType.RESET_PASSWORD -> showDialogToResetPassword(
                nonNull
            )
            HomeState.FolderActionConfirmation.FolderActionType.DETAIL -> maybeShowConfirmPasswordToViewDetailFolder(
                nonNull
            )
            HomeState.FolderActionConfirmation.FolderActionType.TAG -> showTagChoose(nonNull)
        }
    }

    private fun showTagChoose(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        val dialog = ChoiceTagDialogFragment()
        dialog.arguments = Bundle().apply {
            putString(ExtraUtils.EXTRA_TITLE, "Choose tag")
            putInt(ExtraUtils.EXTRA_POSITION, folder.tag ?: 0)
        }
        dialog.show(supportFragmentManager, "dialog")
    }

    private fun maybeShowConfirmPasswordToOpenFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (!folder.isHasPassword()) {
            viewModel.openFolderAfterPasswordVerified(false)
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun maybeShowConfirmPasswordToDeleteFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        val title = getString(R.string.confirmation)
        val numberOfShare = resources.getQuantityString(
            R.plurals.share_count_text, confirmation.shareCount, confirmation.shareCount
        )
        val message = HtmlCompat.fromHtml(
            getString(
                R.string.delete_folder_confirmation_message, folder.name, numberOfShare
            ), HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        showAlert(title,
            message,
            getString(R.string.delete),
            getString(R.string.cancel),
            onPosClickListener = { _, _ ->
                if (folder.isHasPassword().not() || confirmation.ignorePassword) {
                    viewModel.deleteFolder(folder)
                } else {
                    showConfirmPasswordDialog(folder.id)
                }
            })
    }

    private fun maybeShowConfirmPasswordToRenameFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.isHasPassword().not() || confirmation.ignorePassword) {
            BaseDialogFragment.showDialog(
                RenameFolderDialogFragment::class, supportFragmentManager
            ) {
                arguments = Bundle().apply {
                    putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
                }
            }
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun showDialogToResetPassword(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        BaseDialogFragment.showDialog(
            ResetPasswordFolderDialogFragment::class, supportFragmentManager
        ) {
            arguments = Bundle().apply {
                putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
            }
        }
        viewModel.clearFolderActionConfirmation()
    }

    private fun maybeShowConfirmPasswordToViewDetailFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.isHasPassword().not() || confirmation.ignorePassword) {
            BaseDialogFragment.showDialog(
                FolderDetailDialogFragment::class, supportFragmentManager
            ) {
                arguments = Bundle().apply {
                    putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
                }
            }
            viewModel.clearFolderActionConfirmation()
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun showConfirmPasswordDialog(id: String) = getState(viewModel) { state ->
        if (state.folderPasswordConfirmRemind.contains(id)) {
            return@getState onPasswordVerified(false)
        }
        BaseDialogFragment.showDialog(
            FolderConfirmPasswordDialogFragment::class, supportFragmentManager
        ) {
            arguments = Bundle().apply {
                putString(ExtraUtils.EXTRA_FOLDER_ID, id)
            }
        }
    }

    override fun onPasswordVerified(isRemindPassword: Boolean) {
        val actionType =
            getState(viewModel) { state -> state.folderActionConfirmation?.folderActionType }
                ?: return viewModel.clearFolderActionConfirmation()
        when (actionType) {
            HomeState.FolderActionConfirmation.FolderActionType.OPEN -> viewModel.openFolderAfterPasswordVerified(
                isRemindPassword
            )
            HomeState.FolderActionConfirmation.FolderActionType.DELETE -> viewModel.deleteFolderAfterPasswordVerified()
            HomeState.FolderActionConfirmation.FolderActionType.RENAME -> viewModel.renameFolderAfterPasswordVerified()
            HomeState.FolderActionConfirmation.FolderActionType.DETAIL -> viewModel.showDetailFolderAfterPasswordVerified()
            else -> {}
        }
    }

    override fun onCancelConfirmPassword() {
        viewModel.clearFolderActionConfirmation()
    }

    override fun onRenameSuccess() {
        viewModel.loadFolders()
    }

    override fun onCancelRename() {
        viewModel.clearFolderActionConfirmation()
    }

    override fun onTagSelected(tagId: Int) {
        viewModel.setFolderTag(tagId)
    }

    private fun showAbout() =
        AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(buildSpannedString {
            append("\n")
            bold {
                append(getString(R.string.developer_title))
            }
            append(" ${getString(R.string.developer_name)}")
            append("\n\n")
            bold {
                append(getString(R.string.contact_title))
            }
            append(" ${getString(R.string.developer_email)}")
        }).setPositiveButton(R.string.button_close, null).setCancelable(true).show()

    private fun showDialogShareToOther(shareId: Int) {
        val shareData = getState(viewModel) { state ->
            state.shareList.firstOrNull { share ->
                share.id == shareId
            }
        } ?: return
        shareHelper.shareToOther(shareData)
    }

    private fun viewImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "image/*")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        startActivity(intent)
    }

    private fun openRecentlyShareWebLink(position: Int) = getState(viewModel) { state ->
        val data = state.shareList.getOrNull(position) ?: return@getState
        val share = data.takeIf { it.shareType == ShareType.WEB.type } ?: return@getState
        gson.runCatching {
            fromJson(
                share.shareInfo, ShareState.ShareInfo.ShareWebLink::class.java
            )
        }.getOrNull()?.url?.let { url ->
            if (appSharePref.isCustomTabEnabled()) {
                appRouter.moveToChromeCustomTab(this, url)
            } else {
                appRouter.moveToBrowser(url)
            }
        }
    }
}
