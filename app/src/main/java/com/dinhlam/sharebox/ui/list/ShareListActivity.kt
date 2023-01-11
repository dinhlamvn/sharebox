package com.dinhlam.sharebox.ui.list

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityShareListBinding
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.ui.list.modelview.ShareListDateModelView
import com.dinhlam.sharebox.ui.list.modelview.ShareListImageModelView
import com.dinhlam.sharebox.ui.list.modelview.ShareListTextModelView
import com.dinhlam.sharebox.ui.list.modelview.ShareListWebLinkModelView
import com.dinhlam.sharebox.utils.ExtraUtils
import com.dinhlam.sharebox.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareListActivity :
    BaseViewModelActivity<ShareListState, ShareListViewModel, ActivityShareListBinding>() {

    private val modelViewsBuilder by lazy { ShareListModelViewsBuilder(this, viewModel, gson) }

    private val shareListAdapter by lazy {
        BaseListAdapter.createAdapter(modelViewsBuilder) {
            withViewType(R.layout.model_view_loading) {
                LoadingViewHolder(this)
            }

            withViewType(R.layout.model_view_share_list_date) {
                ShareListDateModelView.ShareListDateViewHolder(this)
            }

            withViewType(R.layout.model_view_share_list_text) {
                ShareListTextModelView.ShareListTextViewHolder(this, { textContent ->
                    val dialog = TextViewerDialogFragment()
                    dialog.arguments = Bundle().apply {
                        putString(Intent.EXTRA_TEXT, textContent)
                    }
                    dialog.show(supportFragmentManager, "TextViewerDialogFragment")
                }, ::showDialogShareToOther)
            }

            withViewType(R.layout.model_view_share_list_web_link) {
                ShareListWebLinkModelView.ShareListWebLinkViewHolder(this, ::showDialogShareToOther)
            }

            withViewType(R.layout.model_view_share_list_image) {
                ShareListImageModelView.ShareListImageViewHolder(this, ::showDialogShareToOther)
            }
        }
    }

    private fun showDialogShareToOther(shareId: Int) {
        val shareData = getState(viewModel) { state ->
            state.shareList.firstOrNull { share ->
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
        viewBinding.recyclerView.adapter = shareListAdapter

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

    override fun onStateChanged(state: ShareListState) {
        shareListAdapter.requestBuildModelViews()
        supportActionBar?.title = state.title
    }
}
