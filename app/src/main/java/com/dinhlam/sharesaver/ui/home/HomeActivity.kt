package com.dinhlam.sharesaver.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseDialogFragment
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseViewModelActivity
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.databinding.ActivityHomeBinding
import com.dinhlam.sharesaver.databinding.MenuItemIconWithTextBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.dp
import com.dinhlam.sharesaver.extensions.dpF
import com.dinhlam.sharesaver.extensions.setupWith
import com.dinhlam.sharesaver.extensions.showAlert
import com.dinhlam.sharesaver.extensions.showToast
import com.dinhlam.sharesaver.modelview.FolderListModelView
import com.dinhlam.sharesaver.router.AppRouter
import com.dinhlam.sharesaver.ui.dialog.folder.confirmpassword.FolderConfirmPasswordDialogFragment
import com.dinhlam.sharesaver.ui.dialog.folder.creator.FolderCreatorDialogFragment
import com.dinhlam.sharesaver.ui.dialog.folder.detail.FolderDetailDialogFragment
import com.dinhlam.sharesaver.ui.dialog.folder.rename.RenameFolderDialogFragment
import com.dinhlam.sharesaver.utils.ExtraUtils
import com.dinhlam.sharesaver.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseViewModelActivity<HomeData, HomeViewModel, ActivityHomeBinding>(),
    FolderCreatorDialogFragment.OnFolderCreatorCallback,
    FolderConfirmPasswordDialogFragment.OnConfirmPasswordCallback,
    RenameFolderDialogFragment.OnConfirmRenameCallback {

    private val modelViewsFactory by lazy { HomeModelViewsFactory(this, viewModel, gson) }

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var appRouter: AppRouter

    override val viewModel: HomeViewModel by viewModels()

    private val homeAdapter = BaseListAdapter.createAdapter {
        withViewType(R.layout.model_view_loading) {
            LoadingViewHolder(this)
        }

        withViewType(R.layout.model_view_folder_list) {
            FolderListModelView.FolderListViewHolder(
                this, viewModel::onFolderClick, ::onFolderLongClick
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

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadFolders()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.consumeOnChange(this, HomeData::toastRes) { toastRes ->
            if (toastRes != 0) {
                showToast(getString(toastRes))
                viewModel.clearToast()
            }
        }

        viewModel.consumeOnChange(this, HomeData::folderActionConfirmation, ::handleFolderAction)

        viewModel.consumeOnChange(this, HomeData::folderToOpen) { folderToOpen ->
            folderToOpen?.let {
                viewModel.clearOpenFolder()
                openFolder(it)
            }
        }
    }

    private fun openFolder(folder: Folder) {
        startActivity(appRouter.shareList(folder.id))
    }

    override fun onDataChanged(data: HomeData) {
        modelViewsFactory.requestBuildModelViews()
        viewBinding.frameProgress.frameContainer.isVisible = data.showProgress
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
            startActivity(appRouter.setting())
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
            withData(viewModel) { homeData -> homeData.folders.getOrNull(position) } ?: return
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
            width, height
        )
        popupView.orientation = LinearLayout.VERTICAL
        popupView.layoutParams = layoutParams

        val bindingItemDelete = MenuItemIconWithTextBinding.inflate(layoutInflater)
        bindingItemDelete.imageIcon.setImageResource(R.drawable.ic_delete)
        bindingItemDelete.textView.text = getString(R.string.delete)
        bindingItemDelete.root.setOnClickListener {
            dismissPopup()
            viewModel.processFolderForDelete(folder)
        }
        popupView.addView(bindingItemDelete.root, layoutParams)

        val bindingItemRename = MenuItemIconWithTextBinding.inflate(layoutInflater)
        bindingItemRename.imageIcon.setImageResource(R.drawable.ic_rename)
        bindingItemRename.textView.text = getString(R.string.rename)
        bindingItemRename.root.setOnClickListener {
            dismissPopup()
            viewModel.processFolderForRename(folder)
        }
        popupView.addView(bindingItemRename.root, layoutParams)

        val bindingItemInfo = MenuItemIconWithTextBinding.inflate(layoutInflater)
        bindingItemInfo.imageIcon.setImageResource(R.drawable.ic_detail)
        bindingItemInfo.textView.text = getString(R.string.details)
        bindingItemInfo.root.setOnClickListener {
            dismissPopup()
            viewModel.processFolderForDetail(folder)
        }
        popupView.addView(bindingItemInfo.root, layoutParams)

        popupWindow.contentView = popupView
        popupWindow.showAsDropDown(clickedView, 0, -clickedView.height / 2)
    }

    private fun handleFolderAction(confirmation: HomeData.FolderActionConfirmation?) {
        val nonNull = confirmation ?: return
        return when (nonNull.folderActionType) {
            HomeData.FolderActionConfirmation.FolderActionType.DELETE -> maybeShowConfirmPasswordToDeleteFolder(
                nonNull
            )
            HomeData.FolderActionConfirmation.FolderActionType.OPEN -> maybeShowConfirmPasswordToOpenFolder(
                nonNull
            )
            HomeData.FolderActionConfirmation.FolderActionType.RENAME -> maybeShowConfirmPasswordToRenameFolder(
                nonNull
            )
            HomeData.FolderActionConfirmation.FolderActionType.DETAIL -> maybeShowConfirmPasswordToViewDetailFolder(
                nonNull
            )
        }
    }

    private fun maybeShowConfirmPasswordToOpenFolder(confirmation: HomeData.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.password.isNullOrEmpty()) {
            viewModel.openFolderAfterPasswordVerified(false)
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun maybeShowConfirmPasswordToDeleteFolder(confirmation: HomeData.FolderActionConfirmation) {
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
                if (folder.password.isNullOrEmpty() || confirmation.ignorePassword) {
                    viewModel.deleteFolder(folder)
                } else {
                    showConfirmPasswordDialog(folder.id)
                }
            })
    }

    private fun maybeShowConfirmPasswordToRenameFolder(confirmation: HomeData.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.password.isNullOrEmpty() || confirmation.ignorePassword) {
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

    private fun maybeShowConfirmPasswordToViewDetailFolder(confirmation: HomeData.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.password.isNullOrEmpty() || confirmation.ignorePassword) {
            BaseDialogFragment.showDialog(
                FolderDetailDialogFragment::class, supportFragmentManager
            ) {
                arguments = Bundle().apply {
                    putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
                }
            }
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun showConfirmPasswordDialog(id: String) = withData(viewModel) { data ->
        if (data.folderPasswordConfirmRemind.contains(id)) {
            return@withData onPasswordVerified(false)
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
            withData(viewModel) { data -> data.folderActionConfirmation?.folderActionType }
                ?: return viewModel.clearFolderActionConfirmation()
        when (actionType) {
            HomeData.FolderActionConfirmation.FolderActionType.OPEN -> viewModel.openFolderAfterPasswordVerified(
                isRemindPassword
            )
            HomeData.FolderActionConfirmation.FolderActionType.DELETE -> viewModel.deleteFolderAfterPasswordVerified()
            HomeData.FolderActionConfirmation.FolderActionType.RENAME -> viewModel.renameFolderAfterPasswordVerified()
            HomeData.FolderActionConfirmation.FolderActionType.DETAIL -> viewModel.showDetailFolderAfterPasswordVerified()
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
}