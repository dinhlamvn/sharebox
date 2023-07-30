package com.dinhlam.sharebox.dialog.guideline

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.databinding.DialogGuidelineBinding

class GuidelineDialogFragment : BaseDialogFragment<DialogGuidelineBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogGuidelineBinding {
        return DialogGuidelineBinding.inflate(inflater, container, false)
    }
}
