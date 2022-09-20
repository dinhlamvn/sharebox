package com.dinhlam.sharesaver.ui.share

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseViewModelActivity
import com.dinhlam.sharesaver.databinding.ActivityShareBinding
import com.dinhlam.sharesaver.extensions.asThe
import com.dinhlam.sharesaver.extensions.getTrimmedText
import com.dinhlam.sharesaver.router.AppRouter
import com.dinhlam.sharesaver.ui.share.modelview.ShareDefaultModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareImageModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareMultipleImageModelView
import com.dinhlam.sharesaver.ui.share.modelview.ShareTextModelView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareReceiveActivity :
    BaseViewModelActivity<ShareData, ShareViewModel, ActivityShareBinding>() {

    private val modelViewsFactory = object : BaseListAdapter.ModelViewsFactory() {
        override fun buildModelViews() = withData(viewModel) { data ->
            when (data.shareInfo) {
                is ShareData.ShareInfo.ShareText -> renderShareTextContent(data.shareInfo)
                is ShareData.ShareInfo.ShareImage -> renderShareImageContent(data.shareInfo)
                is ShareData.ShareInfo.ShareMultipleImage -> renderShareMultipleImageContent(data.shareInfo)
                else -> renderShareContentDefault()
            }
        }

        private fun renderShareContentDefault() {
            ShareDefaultModelView().attachTo(this)
        }

        private fun renderShareTextContent(shareText: ShareData.ShareInfo.ShareText) {
            ShareTextModelView("shareText", shareText.text).attachTo(this)
        }

        private fun renderShareImageContent(shareImage: ShareData.ShareInfo.ShareImage) {
            ShareImageModelView("shareImage", shareImage.uri).attachTo(this)
        }

        private fun renderShareMultipleImageContent(shareMultipleImage: ShareData.ShareInfo.ShareMultipleImage) {
            shareMultipleImage.uris.mapIndexed { index, uri ->
                ShareMultipleImageModelView(
                    "shareMultipleImage$index", uri
                )
            }.forEach { it.attachTo(this) }
        }


    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                finish()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val alpha = slideOffset.times(255).coerceAtMost(153f).toInt()
            viewBinding.viewBackground.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
        }
    }

    @Inject
    lateinit var appRouter: AppRouter

    private val behavior: BottomSheetBehavior<View> by lazy {
        BottomSheetBehavior.from(viewBinding.frameContainer)
    }

    override fun onCreateViewBinding(): ActivityShareBinding {
        return ActivityShareBinding.inflate(layoutInflater)
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
            else -> null
        }
    }

    override fun onDataChanged(data: ShareData) {
        modelViewsFactory.requestBuildModelViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding.recyclerView.adapter = shareContentAdapter
        modelViewsFactory.attach(shareContentAdapter)

        behavior.addBottomSheetCallback(bottomSheetCallback)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

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
        viewModel.setShareInfo(ShareData.ShareInfo.ShareText(intent.getStringExtra(Intent.EXTRA_TEXT)))
    }

    private fun handleSendImage(intent: Intent) {
        intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM).asThe<Parcelable, Uri>()
            ?.let { shareUri ->
                viewModel.setShareInfo(ShareData.ShareInfo.ShareImage(shareUri))
            }
    }

    private fun handleSendMultipleImage(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let { list ->
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