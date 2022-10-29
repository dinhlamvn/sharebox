package com.dinhlam.sharesaver.ui.list

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseViewModelActivity
import com.dinhlam.sharesaver.databinding.ActivityShareListBinding
import com.dinhlam.sharesaver.extensions.setupWith
import com.dinhlam.sharesaver.extensions.showToast
import com.dinhlam.sharesaver.helper.ShareHelper
import com.dinhlam.sharesaver.ui.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharesaver.ui.home.modelview.HomeDateModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeImageModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeTextModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeWebLinkModelView
import com.dinhlam.sharesaver.utils.ExtraUtils
import com.dinhlam.sharesaver.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareListActivity :
    BaseViewModelActivity<ShareListState, ShareListViewModel, ActivityShareListBinding>() {

    private val modelViewsFactory by lazy { ShareListModelViewsFactory(this, viewModel, gson) }

    private val shareListAdapter = BaseListAdapter.createAdapter {
        withViewType(R.layout.model_view_loading) {
            LoadingViewHolder(this)
        }

        withViewType(R.layout.model_view_home_date) {
            HomeDateModelView.HomeDateViewHolder(this)
        }

        withViewType(R.layout.model_view_home_share_text) {
            HomeTextModelView.HomeTextViewHolder(this, { textContent ->
                val dialog = TextViewerDialogFragment()
                dialog.arguments = Bundle().apply {
                    putString(Intent.EXTRA_TEXT, textContent)
                }
                dialog.show(supportFragmentManager, "TextViewerDialogFragment")
            }, ::showDialogShareToOther)
        }

        withViewType(R.layout.model_view_home_share_web_link) {
            HomeWebLinkModelView.HomeWebLinkViewHolder(this, ::showDialogShareToOther)
        }

        withViewType(R.layout.model_view_home_share_image) {
            HomeImageModelView.HomeImageViewHolder(this, ::showDialogShareToOther)
        }
    }

    private fun showDialogShareToOther(shareId: Int) {
        val shareData = withState(viewModel) { data ->
            data.shareList.firstOrNull { share ->
                share.id == shareId
            }
        } ?: return
        shareHelper.shareToOther(shareData)
    }

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var shareHelper: ShareHelper

    override fun onCreateViewBinding(): ActivityShareListBinding {
        return ActivityShareListBinding.inflate(layoutInflater)
    }

    override val viewModel: ShareListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.recyclerView.setupWith(shareListAdapter, modelViewsFactory)

        val folderId = intent.getStringExtra(ExtraUtils.EXTRA_FOLDER_ID) ?: return run {
            showToast(R.string.error_require_folder)
            finish()
        }
        viewModel.setFolderId(folderId)

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadShareList()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDataChanged(data: ShareListState) {
        modelViewsFactory.requestBuildModelViews()
        supportActionBar?.title = data.title
    }
}