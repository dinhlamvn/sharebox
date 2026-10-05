package com.dinhlam.sharebox.ui.transfer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityTransferBinding
import com.dinhlam.sharebox.router.Router
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransferActivity : BaseActivity<ActivityTransferBinding>() {
    private val viewModel: TransferViewModel by viewModels()
    @Inject lateinit var router: Router
    private val boxId get() = intent.getStringExtra(AppExtras.EXTRA_BOX_ID)
    private val save = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) boxId?.let { viewModel.exportLocal(it, uri) }
    }
    private val open = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importLocal(uri)
    }
    override fun onCreateViewBinding() = ActivityTransferBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        binding.buttonExport.isVisible = boxId != null
        binding.buttonUpload.isVisible = boxId != null
        binding.copySection.isVisible = boxId != null
        binding.buttonExport.setOnClickListener { boxId?.let { save.launch("box-$it.sharebox") } }
        binding.buttonImport.setOnClickListener { open.launch(arrayOf("*/*")) }
        binding.buttonUpload.setOnClickListener { boxId?.let(viewModel::exportCloud) }
        binding.buttonOffline.setOnClickListener { boxId?.let(viewModel::keepOffline) }
        binding.buttonFork.setOnClickListener { boxId?.let(viewModel::makeCopy) }
        binding.buttonDownload.setOnClickListener { showTransferCodeDialog() }
        binding.buttonCopyCode.setOnClickListener {
            viewModel.state.value.code?.let {
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                    .setPrimaryClip(ClipData.newPlainText("ShareBox transfer", it))
            }
        }
        binding.buttonOpenBox.setOnClickListener {
            viewModel.state.value.boxId?.let { startActivity(router.boxDetail(this, it)) }
        }
        val actions = listOf(binding.buttonExport, binding.buttonImport, binding.buttonUpload,
            binding.buttonOffline, binding.buttonFork, binding.buttonDownload, binding.buttonCopyCode, binding.buttonOpenBox)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    actions.forEach { it.isEnabled = !state.busy }
                    binding.progressGroup.isVisible = state.busy
                    binding.message.isVisible = state.message != null
                    binding.message.text = listOfNotNull(state.message, state.code).joinToString("\n\n")
                    binding.buttonCopyCode.isVisible = state.code != null
                    binding.buttonOpenBox.isVisible = state.boxId != null
                }
            }
        }
    }

    private fun showTransferCodeDialog() {
        val padding = (24 * resources.displayMetrics.density).toInt()
        val container = TextInputLayout(this).apply {
            setPadding(padding, padding / 2, padding, 0)
            hint = getString(R.string.transfer_code_hint)
        }
        val input = TextInputEditText(container.context).apply { isSingleLine = true }
        container.addView(input)
        val dialog = MaterialAlertDialogBuilder(this).setTitle(R.string.library_download_code)
            .setView(container).setPositiveButton(R.string.download, null)
            .setNegativeButton(R.string.cancel, null).create()
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val code = input.text.toString().trim()
                if (code.isBlank()) container.error = getString(R.string.transfer_code_hint)
                else { viewModel.importCloud(code); dialog.dismiss() }
            }
        }
        dialog.show()
    }
}
