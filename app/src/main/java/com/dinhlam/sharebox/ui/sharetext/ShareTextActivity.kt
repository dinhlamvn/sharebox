package com.dinhlam.sharebox.ui.sharetext

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.lifecycleScope
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.databinding.ActivityShareTextBinding
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.showKeyboard
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.utils.Icons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShareTextActivity : BaseActivity<ActivityShareTextBinding>() {

    override fun onCreateViewBinding(): ActivityShareTextBinding {
        return ActivityShareTextBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }

    private fun onDone() {
        binding.editTextQuote.hideKeyboard()
        val text = binding.editTextQuote.getTrimmedText().takeIfNotNullOrBlank()
            ?: return showToast(R.string.require_input_text_quote)
        setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_TEXT, text))
        finish()
    }
}