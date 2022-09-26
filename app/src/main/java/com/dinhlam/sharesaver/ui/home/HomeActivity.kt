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
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseSpanSizeLookup
import com.dinhlam.sharesaver.base.BaseViewModelActivity
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.databinding.ActivityMainBinding
import com.dinhlam.sharesaver.databinding.MenuItemIconWithTextBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.dp
import com.dinhlam.sharesaver.extensions.dpF
import com.dinhlam.sharesaver.extensions.registerOnBackPressHandler
import com.dinhlam.sharesaver.extensions.setupWith
import com.dinhlam.sharesaver.extensions.showAlert
import com.dinhlam.sharesaver.extensions.showToast
import com.dinhlam.sharesaver.modelview.FolderModelView
import com.dinhlam.sharesaver.ui.dialog.folder.confirmpassword.FolderConfirmPasswordDialogFragment
import com.dinhlam.sharesaver.ui.dialog.folder.creator.FolderCreatorDialogFragment
import com.dinhlam.sharesaver.ui.home.modelview.HomeDateModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeImageModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeWebLinkModelView
import com.dinhlam.sharesaver.utils.ExtraUtils
import com.dinhlam.sharesaver.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseViewModelActivity<HomeData, HomeViewModel, ActivityMainBinding>(),
    FolderCreatorDialogFragment.OnFolderCreatorCallback,
    FolderConfirmPasswordDialogFragment.OnConfirmPasswordCallback {

    private val modelViewsFactory by lazy { HomeModelViewsFactory(this, viewModel, gson) }

    companion object {
        private const val SPAN_COUNT = 3
    }

    @Inject
    lateinit var gson: Gson

    override val viewModel: HomeViewModel by viewModels()

    private val homeAdapter = BaseListAdapter.createAdapter {
        withViewType(R.layout.model_view_loading) {
            LoadingViewHolder(this)
        }

        withViewType(R.layout.model_view_home_share_web_link) {
            HomeWebLinkModelView.HomeTextViewHolder(this)
        }

        withViewType(R.layout.model_view_home_share_image) {
            HomeImageModelView.HomeImageViewHolder(this)
        }

        withViewType(R.layout.model_view_folder) {
            FolderModelView.FolderViewHolder(this, viewModel::onFolderClick, ::onFolderLongClick)
        }

        withViewType(R.layout.model_view_home_date) {
            HomeDateModelView.HomeDateViewHolder(this)
        }
    }

    override fun onCreateViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerOnBackPressHandler {
            if (viewModel.handleBackPressed()) {
                return@registerOnBackPressHandler
            }
            finish()
        }

        viewBinding.recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT).apply {
            spanSizeLookup = BaseSpanSizeLookup(homeAdapter, this)
        }

        viewBinding.recyclerView.setupWith(homeAdapter, modelViewsFactory)

        viewBinding.recyclerView.adapter = homeAdapter
        modelViewsFactory.attach(homeAdapter)

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.reload()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.consumeOnChange(HomeData::toastRes) { toastRes ->
            if (toastRes != 0) {
                showToast(getString(toastRes))
                viewModel.clearToast()
            }
        }

        viewModel.consumeOnChange(HomeData::folderActionConfirmation, ::handleFolderAction)
    }

    override fun onDataChanged(data: HomeData) {
        modelViewsFactory.requestBuildModelViews()
        setupUiOnFolderSelected(data.selectedFolder)
        viewBinding.frameProgress.frameContainer.isVisible = data.showProgress
    }

    private fun setupUiOnFolderSelected(folder: Folder?) {
        if (folder != null) {
            val title = folder.name
            supportActionBar?.title = title
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            supportActionBar?.title = getString(R.string.app_name)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
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
        if (item.itemId == android.R.id.home) {
            return viewModel.handleBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialogCreateFolder() {
        val dialog = FolderCreatorDialogFragment()
        dialog.show(supportFragmentManager, "DialogCreateFolder")
    }

    override fun onFolderCreated(folderId: String) {
        viewModel.reload()
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
            viewModel.showConfirmDeleteFolder(folder)
        }
        popupView.addView(bindingItemDelete.root, layoutParams)

        val bindingItemRename = MenuItemIconWithTextBinding.inflate(layoutInflater)
        bindingItemRename.imageIcon.setImageResource(R.drawable.ic_rename)
        bindingItemRename.textView.text = getString(R.string.rename)
        bindingItemRename.root.setOnClickListener {
            dismissPopup()
        }
        popupView.addView(bindingItemRename.root, layoutParams)

        popupWindow.contentView = popupView
        popupWindow.showAsDropDown(clickedView, 0, -clickedView.height / 2)
    }

    private fun handleFolderAction(confirmation: HomeData.FolderActionConfirmation?) {
        val nonNull = confirmation ?: return
        if (nonNull.folderActionType == HomeData.FolderActionConfirmation.FolderActionType.DELETE) {
            return maybeShowConfirmPasswordToDeleteFolder(nonNull)
        }

        if (nonNull.folderActionType == HomeData.FolderActionConfirmation.FolderActionType.OPEN) {
            return maybeShowConfirmPasswordToOpenFolder(nonNull)
        }
    }

    private fun maybeShowConfirmPasswordToOpenFolder(confirmation: HomeData.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.password.isNullOrEmpty()) {
            viewModel.openFolderAfterPasswordVerified()
        } else {
            val dialog = FolderConfirmPasswordDialogFragment()
            dialog.arguments = Bundle().apply {
                putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
            }
            dialog.show(supportFragmentManager, "DialogConfirmPassword")
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
                if (folder.password.isNullOrEmpty()) {
                    viewModel.deleteFolder(folder)
                } else {
                    val dialog = FolderConfirmPasswordDialogFragment()
                    dialog.arguments = Bundle().apply {
                        putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
                    }
                    dialog.show(supportFragmentManager, "DialogConfirmPassword")
                }
            })
    }

    override fun onPasswordVerified() {
        val actionType =
            withData(viewModel) { data -> data.folderActionConfirmation?.folderActionType }
                ?: return viewModel.clearFolderActionConfirmation()
        when (actionType) {
            HomeData.FolderActionConfirmation.FolderActionType.OPEN -> viewModel.openFolderAfterPasswordVerified()
            HomeData.FolderActionConfirmation.FolderActionType.DELETE -> viewModel.deleteFolderAfterPasswordVerified()
            else -> return
        }
    }

    override fun onCancelConfirmPassword() {
        viewModel.clearFolderActionConfirmation()
    }
}