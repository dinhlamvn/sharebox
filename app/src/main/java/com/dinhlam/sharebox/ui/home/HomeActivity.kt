package com.dinhlam.sharebox.ui.home

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.databinding.MenuItemWithTextBinding
import com.dinhlam.sharebox.dialog.folder.confirmpassword.FolderConfirmPasswordDialogFragment
import com.dinhlam.sharebox.dialog.folder.creator.FolderCreatorDialogFragment
import com.dinhlam.sharebox.dialog.folder.detail.FolderDetailDialogFragment
import com.dinhlam.sharebox.dialog.folder.rename.RenameFolderDialogFragment
import com.dinhlam.sharebox.dialog.guideline.GuidelineDialogFragment
import com.dinhlam.sharebox.dialog.tag.ChoiceTagDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.dpF
import com.dinhlam.sharebox.extensions.getSerializableExtraCompat
import com.dinhlam.sharebox.extensions.setupWith
import com.dinhlam.sharebox.extensions.showAlert
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.modelview.FolderListModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.utils.ExtraUtils
import com.dinhlam.sharebox.utils.FolderUtils
import com.dinhlam.sharebox.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class HomeActivity :
    BaseViewModelActivity<HomeState, HomeViewModel, ActivityHomeBinding>(),
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

    private val modelViewsFactory by lazy { HomeModelViewsFactory(this, viewModel, gson) }

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var appSharePref: AppSharePref

    override val viewModel: HomeViewModel by viewModels()

    private val homeAdapter = BaseListAdapter.createAdapter {
        withViewType(R.layout.model_view_loading) {
            LoadingViewHolder(this)
        }

        withViewType(R.layout.model_view_folder_list) {
            FolderListModelView.FolderListViewHolder(
                this,
                viewModel::onFolderClick,
                ::onFolderLongClick
            )
        }
    }

    override fun onCreateViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        viewBinding.recyclerView.setupWith(homeAdapter, modelViewsFactory)

        viewModel.setSortType(appSharePref.getSortType())

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadFolders()
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

        viewBinding.buttonGuideLine.setOnClickListener {
            openGuideLineDialog()
        }
    }

    private fun openGuideLineDialog() {
        BaseDialogFragment.showDialog(GuidelineDialogFragment::class, supportFragmentManager)
    }

    private fun openFolder(folder: Folder) {
        startActivity(appRouter.shareList(folder.id))
    }

    override fun onStateChanged(state: HomeState) {
        modelViewsFactory.requestBuildModelViews()
        viewBinding.frameProgress.frameContainer.isVisible = state.showProgress
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.item_clear_tag)?.isVisible = withState(viewModel) { it.tag != null }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu.cast<MenuBuilder>()?.setOptionalIconsVisible(true)
        return menuInflater.inflate(R.menu.home_menu, menu).let { true }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_create_folder) {
            showDialogCreateFolder()
            return true
        }
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

        return super.onOptionsItemSelected(item)
    }

    private fun showDialogCreateFolder() {
        val dialog = FolderCreatorDialogFragment()
        dialog.show(supportFragmentManager, "DialogCreateFolder")
    }

    override fun onFolderCreated(folderId: String) {
        viewModel.loadFolders()
    }

    private fun onFolderLongClick(clickedView: View, position: Int) {
        val folder =
            withState(viewModel) { homeData -> homeData.folders.getOrNull(position) } ?: return
        var popupSpacing = if (folder.tag == null) 0 else POPUP_ITEM_SPACING

        val width = 150.dp(this)
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(this)
        popupWindow.width = width
        popupWindow.height = height
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 10.dpF(this)
        popupWindow.isOutsideTouchable = true
        fun dismissPopup() {
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        }

        val popupView = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(
            width,
            height
        )
        popupView.orientation = LinearLayout.VERTICAL
        popupView.layoutParams = layoutParams

        if (!FolderUtils.isProtectedFolder(folder.id)) {
            val bindingItemDelete = MenuItemWithTextBinding.inflate(layoutInflater)
            bindingItemDelete.textView.text = getString(R.string.delete)
            bindingItemDelete.root.setOnClickListener {
                dismissPopup()
                viewModel.processFolderForDelete(folder)
            }
            popupView.addView(bindingItemDelete.root, layoutParams)
            popupSpacing += POPUP_ITEM_SPACING

            val bindingItemRename = MenuItemWithTextBinding.inflate(layoutInflater)
            bindingItemRename.textView.text = getString(R.string.rename)
            bindingItemRename.root.setOnClickListener {
                dismissPopup()
                viewModel.processFolderForRename(folder)
            }
            popupView.addView(bindingItemRename.root, layoutParams)
            popupSpacing += POPUP_ITEM_SPACING
        }

        val bindingItemTag = MenuItemWithTextBinding.inflate(layoutInflater)
        bindingItemTag.textView.text = getString(R.string.add_tag)
        bindingItemTag.root.setOnClickListener {
            dismissPopup()
            viewModel.processFolderForTag(folder)
        }
        popupView.addView(bindingItemTag.root, layoutParams)
        popupSpacing += POPUP_ITEM_SPACING

        if (folder.tag != null) {
            val bindingItemRemoveTag = MenuItemWithTextBinding.inflate(layoutInflater)
            bindingItemRemoveTag.textView.text = getString(R.string.remove_tag)
            bindingItemRemoveTag.root.setOnClickListener {
                dismissPopup()
                viewModel.removeTag(folder)
            }
            popupView.addView(bindingItemRemoveTag.root, layoutParams)
            popupSpacing += POPUP_ITEM_SPACING
        }

        val bindingItemInfo = MenuItemWithTextBinding.inflate(layoutInflater)
        bindingItemInfo.textView.text = getString(R.string.details)
        bindingItemInfo.root.setOnClickListener {
            dismissPopup()
            viewModel.processFolderForDetail(folder)
        }
        popupView.addView(bindingItemInfo.root, layoutParams)
        popupSpacing += POPUP_ITEM_SPACING

        popupWindow.contentView = popupView

        clickedView.post {
            val bottom = clickedView.bottom
            val parentBottom = viewBinding.recyclerView.bottom
            val yOffset = min(0, parentBottom - (bottom + popupSpacing.dp(this)))
            popupWindow.showAsDropDown(clickedView, 0, yOffset)
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
        if (folder.password.isNullOrEmpty()) {
            viewModel.openFolderAfterPasswordVerified(false)
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun maybeShowConfirmPasswordToDeleteFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        val title = getString(R.string.confirmation)
        val numberOfShare = resources.getQuantityString(
            R.plurals.share_count_text,
            confirmation.shareCount,
            confirmation.shareCount
        )
        val message = HtmlCompat.fromHtml(
            getString(
                R.string.delete_folder_confirmation_message,
                folder.name,
                numberOfShare
            ),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        showAlert(
            title,
            message,
            getString(R.string.delete),
            getString(R.string.cancel),
            onPosClickListener = { _, _ ->
                if (folder.password.isNullOrEmpty() || confirmation.ignorePassword) {
                    viewModel.deleteFolder(folder)
                } else {
                    showConfirmPasswordDialog(folder.id)
                }
            }
        )
    }

    private fun maybeShowConfirmPasswordToRenameFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.password.isNullOrEmpty() || confirmation.ignorePassword) {
            BaseDialogFragment.showDialog(
                RenameFolderDialogFragment::class,
                supportFragmentManager
            ) {
                arguments = Bundle().apply {
                    putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
                }
            }
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun maybeShowConfirmPasswordToViewDetailFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.password.isNullOrEmpty() || confirmation.ignorePassword) {
            BaseDialogFragment.showDialog(
                FolderDetailDialogFragment::class,
                supportFragmentManager
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

    private fun showConfirmPasswordDialog(id: String) = withState(viewModel) { data ->
        if (data.folderPasswordConfirmRemind.contains(id)) {
            return@withState onPasswordVerified(false)
        }
        BaseDialogFragment.showDialog(
            FolderConfirmPasswordDialogFragment::class,
            supportFragmentManager
        ) {
            arguments = Bundle().apply {
                putString(ExtraUtils.EXTRA_FOLDER_ID, id)
            }
        }
    }

    override fun onPasswordVerified(isRemindPassword: Boolean) {
        val actionType =
            withState(viewModel) { data -> data.folderActionConfirmation?.folderActionType }
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

    private fun showAbout() = AlertDialog.Builder(this)
        .setTitle(R.string.app_name)
        .setMessage(buildSpannedString {
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
        })
        .setPositiveButton(R.string.button_close, null)
        .setCancelable(true)
        .show()
}
