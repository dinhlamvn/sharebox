package com.dinhlam.sharesaver.ui.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
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
import com.dinhlam.sharesaver.ui.share.viewholder.ShareDefaultViewHolder
import com.dinhlam.sharesaver.ui.share.viewholder.ShareImageViewHolder
import com.dinhlam.sharesaver.ui.share.viewholder.ShareMultipleImageViewHolder
import com.dinhlam.sharesaver.ui.share.viewholder.ShareTextViewHolder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareReceiveActivity :
    BaseViewModelActivity<ShareData, ShareViewModel, ActivityShareBinding>() {

    @Inject
    lateinit var appRouter: AppRouter

    override fun onCreateViewBinding(): ActivityShareBinding {
        return ActivityShareBinding.inflate(layoutInflater)
    }

    override val viewModel: ShareViewModel by viewModels()

    private val shareContentAdapter = BaseListAdapter.createAdapter { viewType, view ->
        when (viewType) {
            R.layout.share_item_default -> ShareDefaultViewHolder(view)
            R.layout.share_item_text -> ShareTextViewHolder(view)
            R.layout.share_item_image -> ShareImageViewHolder(view)
            R.layout.share_item_multiple_image -> ShareMultipleImageViewHolder(view)
            else -> throw NullPointerException("No view holder")
        }
    }

    override fun onDataChanged(data: ShareData) {
        when (data.shareInfo) {
            is ShareData.ShareInfo.ShareText -> renderShareTextContent(data.shareInfo)
            is ShareData.ShareInfo.ShareImage -> renderShareImageContent(data.shareInfo)
            is ShareData.ShareInfo.ShareMultipleImage -> renderShareMultipleImageContent(data.shareInfo)
            else -> renderShareContentDefault()
        }
    }

    private fun renderShareContentDefault() {
        shareContentAdapter.buildModelViews {
            add(ShareDefaultModelView())
        }
    }

    private fun renderShareTextContent(shareText: ShareData.ShareInfo.ShareText) {
        shareContentAdapter.buildModelViews {
            add(ShareTextModelView("shareText", shareText.text.orEmpty()))
        }
    }

    private fun renderShareImageContent(shareImage: ShareData.ShareInfo.ShareImage) {
        shareContentAdapter.buildModelViews {
            add(ShareImageModelView("shareImage", shareImage.uri))
        }
    }

    private fun renderShareMultipleImageContent(shareMultipleImage: ShareData.ShareInfo.ShareMultipleImage) {
        shareContentAdapter.buildModelViews {
            addAll(shareMultipleImage.uris.mapIndexed { index, uri ->
                ShareMultipleImageModelView(
                    "shareMultipleImage$index",
                    uri
                )
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding.recyclerView.adapter = shareContentAdapter

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
                finish()
            }
        }

        viewBinding.buttonCancel.setOnClickListener {
            finish()
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
}