package com.dinhlam.sharesaver.ui.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseViewModelActivity
import com.dinhlam.sharesaver.databinding.ActivityShareReceiveBinding
import com.dinhlam.sharesaver.extensions.asThe
import com.dinhlam.sharesaver.extensions.getParcelableArrayListExtraCompat
import com.dinhlam.sharesaver.extensions.getParcelableExtraCompat
import com.dinhlam.sharesaver.extensions.getTrimmedText
import com.dinhlam.sharesaver.extensions.isWebLink
import com.dinhlam.sharesaver.extensions.showToast
import com.dinhlam.sharesaver.router.AppRouter
import com.dinhlam.sharesaver.ui.share.modelview.ShareDefaultModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareImageModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareMultipleImageModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareTextModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareWebLinkModelView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareReceiveActivity :
    BaseViewModelActivity<ShareData, ShareViewModel, ActivityShareReceiveBinding>() {

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

    private val shareContentAdapter = BaseListAdapter.createAdapter { viewType, view ->
        when (viewType) {
            R.layout.share_item_default -> ShareDefaultModelView.ShareDefaultViewHolder(view)
            R.layout.share_item_text -> ShareTextModelView.ShareTextViewHolder(view)
            R.layout.share_item_image -> ShareImageModelView.ShareImageViewHolder(view)
            R.layout.share_item_multiple_image -> ShareMultipleImageModelView.ShareMultipleImageViewHolder(
                view
            )
            R.layout.model_view_share_web_link -> ShareWebLinkModelView.ShareWebLinkViewHolder(view)
            else -> null
        }
    }

    override fun onDataChanged(data: ShareData) {
        modelViewsFactory.requestBuildModelViews()
        viewBinding.textViewFolder.text = data.selectedFolder?.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding.recyclerView.adapter = shareContentAdapter
        modelViewsFactory.attach(shareContentAdapter)

        behavior.addBottomSheetCallback(bottomSheetCallback)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        viewBinding.viewBackground.setOnClickListener { dismiss() }

        viewBinding.textViewFolder.setOnClickListener {
            showToast("Select folder")
        }

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

        viewModel.consumeOnChange(ShareData::isSaveSuccess) { isSaveSuccess ->
            if (isSaveSuccess) {
                Toast.makeText(this, R.string.save_share_successfully, Toast.LENGTH_SHORT).show()
                dismiss()
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
        viewModel.setShareInfo(ShareData.ShareInfo.None)
    }

    private fun handleSendText(intent: Intent) {
        val shareContent = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        val shareInfo = when {
            shareContent.isWebLink() -> ShareData.ShareInfo.ShareWebLink(shareContent)
            else -> ShareData.ShareInfo.ShareText(shareContent)
        }
        viewModel.setShareInfo(shareInfo)
    }

    private fun handleSendImage(intent: Intent) {
        intent.getParcelableExtraCompat<Parcelable>(Intent.EXTRA_STREAM)
            .asThe<Parcelable, Uri>()?.let { shareUri ->
                viewModel.setShareInfo(ShareData.ShareInfo.ShareImage(shareUri))
            }
    }

    private fun handleSendMultipleImage(intent: Intent) {
        intent.getParcelableArrayListExtraCompat<Parcelable>(Intent.EXTRA_STREAM)
            ?.let { list ->
                val data = list.mapNotNull { it.asThe<Parcelable, Uri>() }
                viewModel.setShareInfo(ShareData.ShareInfo.ShareMultipleImage(data))
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
}