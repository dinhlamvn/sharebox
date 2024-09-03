package com.dinhlam.sharebox.ui.textinput

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityTextInputBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.showKeyboard
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TextInputActivity :
    BaseActivity<ActivityTextInputBinding>() {

    override fun onCreateViewBinding(): ActivityTextInputBinding {
        return ActivityTextInputBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.title =
            intent.getStringExtra(AppExtras.EXTRA_TITLE) ?: getString(R.string.note)
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

        binding.editTextQuote.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
        binding.editTextQuote.requestFocus()
        lifecycleScope.launch {
            delay(500)
            binding.editTextQuote.showKeyboard()
        }
    }

    private fun onDone() {
        hideKeyboard()
        AlertDialog.Builder(this)
            .setMessage(R.string.confirm_save_message)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                val text = binding.editTextQuote.getTrimmedText()
                setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_TEXT, text))
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}