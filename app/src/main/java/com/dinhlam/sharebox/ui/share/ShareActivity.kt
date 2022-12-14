package com.dinhlam.sharebox.ui.share

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityShareReceiveBinding
import com.dinhlam.sharebox.databinding.MenuItemAddBinding
import com.dinhlam.sharebox.databinding.MenuItemFolderSelectorBinding
import com.dinhlam.sharebox.databinding.MenuItemMoreBinding
import com.dinhlam.sharebox.dialog.folder.creator.FolderCreatorDialogFragment
import com.dinhlam.sharebox.dialog.folder.selector.FolderSelectorDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.dpF
import com.dinhlam.sharebox.extensions.getParcelableArrayListExtraCompat
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.setupWith
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.ui.share.modelview.ShareDefaultModelView
import com.dinhlam.sharebox.ui.share.modelview.ShareImageModelView
import com.dinhlam.sharebox.ui.share.modelview.ShareMultipleImageModelView
import com.dinhlam.sharebox.ui.share.modelview.ShareTextModelView
import com.dinhlam.sharebox.ui.share.modelview.ShareWebLinkModelView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareActivity :
    BaseViewModelActivity<ShareState, ShareViewModel, ActivityShareReceiveBinding>(),
    FolderSelectorDialogFragment.OnFolderSelectorCallback,
    FolderCreatorDialogFragment.OnFolderCreatorCallback {

    companion object {
        private const val LIMIT_SHOWED_FOLDER = 3
    }

    private val modelViewsFactory by lazy { ShareModelViewsFactory(this, viewModel) }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                finish()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }

    @Inject
    lateinit var appRouter: AppRouter

    private val behavior: BottomSheetBehavior<View> by lazy {
        BottomSheetBehavior.from(viewBinding.frameContainer)
    }

    override fun onCreateViewBinding(): ActivityShareReceiveBinding {
        return ActivityShareReceiveBinding.inflate(layoutInflater)
    }

    override val viewModel: ShareViewModel by viewModels()

    private val shareContentAdapter = BaseListAdapter.createAdapter {
        withViewType(R.layout.share_item_default) {
            ShareDefaultModelView.ShareDefaultViewHolder(this)
        }

        withViewType(R.layout.share_item_text) {
            ShareTextModelView.ShareTextViewHolder(this)
        }

        withViewType(R.layout.share_item_image) {
            ShareImageModelView.ShareImageViewHolder(this)
        }

        withViewType(R.layout.share_item_multiple_image) {
            ShareMultipleImageModelView.ShareMultipleImageViewHolder(this)
        }

        withViewType(R.layout.model_view_share_web_link) {
            ShareWebLinkModelView.ShareWebLinkViewHolder(this)
        }
    }

    override fun onStateChanged(state: ShareState) {
        modelViewsFactory.requestBuildModelViews()
        viewBinding.textViewFolder.text = state.selectedFolder?.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding.recyclerView.setupWith(shareContentAdapter, modelViewsFactory)

        behavior.addBottomSheetCallback(bottomSheetCallback)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        viewBinding.viewBackground.setOnClickListener { dismiss() }

        viewBinding.textViewFolder.setOnClickListener(::showFloatingPopupFolderPicker)

        when {
            intent.action == Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    handleSendText(intent)
                } else if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent)
                } else {
                    openHome()
                }
            }
            intent.action == Intent.ACTION_SEND_MULTIPLE && intent.type?.startsWith("image/") == true -> {
                handleSendMultipleImage(intent)
            }
            else -> handleSendNoThing()
        }

        viewModel.consume(this, ShareState::isSaveSuccess) { isSaveSuccess ->
            if (isSaveSuccess) {
                Toast.makeText(this, R.string.save_share_successfully, Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        viewModel.consume(this, ShareState::selectedFolder) { folder ->
            folder?.id?.let { folderId ->
                viewModel.saveLastSelectedFolder(folderId)
            }
        }

        viewBinding.buttonCancel.setOnClickListener {
            dismiss()
        }

        viewBinding.buttonSave.setOnClickListener {
            viewModel.saveShare(viewBinding.textInputNote.getTrimmedText(), this)
        }
    }

    private fun handleSendNoThing() {
        viewModel.setShareInfo(ShareState.ShareInfo.None)
    }

    private fun handleSendText(intent: Intent) {
        val shareContent = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        val shareInfo = when {
            shareContent.isWebLink() -> ShareState.ShareInfo.ShareWebLink(shareContent)
            else -> ShareState.ShareInfo.ShareText(shareContent)
        }
        viewModel.setShareInfo(shareInfo)
    }

    private fun handleSendImage(intent: Intent) {
        intent.getParcelableExtraCompat<Parcelable>(Intent.EXTRA_STREAM).cast<Uri>()
            ?.let { shareUri ->
                viewModel.setShareInfo(ShareState.ShareInfo.ShareImage(shareUri))
            }
    }

    private fun handleSendMultipleImage(intent: Intent) {
        intent.getParcelableArrayListExtraCompat<Parcelable>(Intent.EXTRA_STREAM)?.let { list ->
            val data = list.mapNotNull { it.cast<Uri>() }
            viewModel.setShareInfo(ShareState.ShareInfo.ShareMultipleImage(data))
        }
    }

    private fun openHome() {
        startActivity(
            appRouter.home()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun dismiss() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showFloatingPopupFolderPicker(view: View) = getState(viewModel) { state ->
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

        val takeFolders = state.folders.filterNot { folder -> folder.id == state.selectedFolder?.id }
            .sortedByDescending { folder -> folder.createdAt }.take(LIMIT_SHOWED_FOLDER)
        val popupView = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(
            width,
            height
        )
        popupView.orientation = LinearLayout.VERTICAL
        popupView.layoutParams = layoutParams

        takeFolders.forEach { folder ->
            val binding = MenuItemFolderSelectorBinding.inflate(layoutInflater)
            binding.textView.text = folder.name
            binding.root.setOnClickListener {
                dismissPopup()
                viewModel.setSelectedFolder(folder.id)
            }
            popupView.addView(binding.root, layoutParams)
        }

        if (takeFolders.size < state.folders.size) {
            val binding = MenuItemMoreBinding.inflate(layoutInflater)
            binding.root.setOnClickListener {
                dismissPopup()
                showPickMoreFolderDialog()
            }
            popupView.addView(binding.root, layoutParams)
        }

        val binding = MenuItemAddBinding.inflate(layoutInflater)
        binding.textView.text = getString(R.string.create_new_folder)
        binding.root.setOnClickListener {
            dismissPopup()
            onCreateNewFolder()
        }
        popupView.addView(binding.root, layoutParams)

        popupWindow.contentView = popupView
        popupWindow.showAsDropDown(view)
    }

    private fun showPickMoreFolderDialog() {
        val dialog = FolderSelectorDialogFragment()
        dialog.show(supportFragmentManager, "picker")
    }

    override fun onFolderSelected(folderId: String) {
        viewModel.setSelectedFolder(folderId)
    }

    override fun onCreateNewFolder() {
        val dialog = FolderCreatorDialogFragment()
        dialog.show(supportFragmentManager, "creator")
    }

    override fun onFolderCreated(folderId: String) {
        viewModel.setSelectedFolderAfterCreate(folderId)
    }
}
