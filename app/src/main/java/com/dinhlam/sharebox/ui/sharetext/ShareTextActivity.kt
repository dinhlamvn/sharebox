package com.dinhlam.sharebox.ui.sharetext

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityShareTextBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.showKeyboard
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareTextActivity :
    BaseViewModelActivity<ShareTextState, ShareTextViewModel, ActivityShareTextBinding>() {

    override fun onCreateViewBinding(): ActivityShareTextBinding {
        return ActivityShareTextBinding.inflate(layoutInflater)
    }

    override val viewModel: ShareTextViewModel by viewModels()

    override fun onStateChanged(state: ShareTextState) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.imageDone.setImageDrawable(Icons.doneIcon(this))

        binding.imageDone.setOnClickListener {
            onDone()
        }

        binding.editTextQuote.setHorizontallyScrolling(false)
        binding.editTextQuote.maxLines = Int.MAX_VALUE

        binding.editTextQuote.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onDone()
            }
            true
        }

        binding.editTextQuote.requestFocus()
        lifecycleScope.launch {
            delay(500)
            binding.editTextQuote.showKeyboard()
        }

        viewModel.onChange(this, ShareTextState::shareDetail) { shareDetail ->
            binding.editTextQuote.setText(shareDetail?.shareData?.cast<ShareData.ShareText>()?.text)
        }

        viewModel.onChange(this, ShareTextState::asyncLoadSave) { asyncLoad ->
            binding.loading.isVisible = asyncLoad is BaseViewModel.AsyncLoad.Loading
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun onDone() = getState(viewModel) { state ->
        hideKeyboard()
        state.shareId?.let(::saveShare) ?: callback()
    }

    private fun saveShare(shareId: String) {
        AlertDialog.Builder(this)
            .setMessage(R.string.confirm_save_message)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.save(shareId, binding.editTextQuote.text?.toString())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun callback() {
        binding.editTextQuote.hideKeyboard()
        val text = binding.editTextQuote.getTrimmedText().takeIfNotNullOrBlank()
            ?: return showToast(R.string.require_input_text_quote)
        setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_TEXT, text))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}