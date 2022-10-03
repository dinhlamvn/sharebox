package com.dinhlam.sharesaver.ui.dialog.folder.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseViewModelDialogFragment
import com.dinhlam.sharesaver.database.entity.Folder
import com.dinhlam.sharesaver.databinding.DialogFolderCreatorBinding
import com.dinhlam.sharesaver.extensions.format
import com.dinhlam.sharesaver.utils.ExtraUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FolderDetailDialogFragment :
    BaseViewModelDialogFragment<FolderDetailDialogData, FolderDetailDialogViewModel, DialogFolderCreatorBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogFolderCreatorBinding {
        return DialogFolderCreatorBinding.inflate(inflater, container, false)
    }

    override val viewModel: FolderDetailDialogViewModel by viewModels()

    override fun onDataChanged(data: FolderDetailDialogData) {

    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        val folderId: String = arguments?.getString(ExtraUtils.EXTRA_FOLDER_ID) ?: return dismiss()
        viewModel.loadFolderData(folderId)

        viewBinding.checkboxPassword.setOnCheckedChangeListener { _, isChecked ->
            viewBinding.textLayoutFolderPassword.isVisible = isChecked
            viewBinding.textLayoutFolderPasswordAlias.isVisible = isChecked
        }

        viewModel.consumeOnChange(FolderDetailDialogData::folder, ::onRenderFolderDetail)
        viewModel.consume(FolderDetailDialogData::shareCount, ::onShareCountChange)
    }

    private fun onShareCountChange(shareCount: Int) {
        viewBinding.textShareCount.text = HtmlCompat.fromHtml(
            getString(
                R.string.share_count_template,
                resources.getQuantityString(R.plurals.share_count_text, shareCount, shareCount)
            ), HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun onRenderFolderDetail(folder: Folder?) {
        val nonNullFolder = folder ?: return
        viewBinding.textCreatedDate.text = HtmlCompat.fromHtml(
            getString(
                R.string.created_date_template,
                nonNullFolder.createdAt.format("yyyy/MM/dd HH:mm:ss")
            ), HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        viewBinding.textUpdatedDate.text = HtmlCompat.fromHtml(
            getString(
                R.string.updated_date_template,
                nonNullFolder.updatedAt.format("yyyy/MM/dd HH:mm:ss")
            ), HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        viewBinding.textInputFolderName.setText(nonNullFolder.name)
        viewBinding.textInputFolderDesc.setText(nonNullFolder.desc)
        viewBinding.textInputFolderPasswordAlias.setText(nonNullFolder.passwordAlias)
        if (!nonNullFolder.password.isNullOrBlank()) {
            viewBinding.checkboxPassword.isChecked = true
            viewBinding.textInputFolderPassword.setText("***")
        }
    }

    override fun getSpacing(): Int {
        return 32
    }
}