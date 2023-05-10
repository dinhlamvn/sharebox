package com.dinhlam.sharebox.ui.list

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseBottomSheetDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityShareListBinding
import com.dinhlam.sharebox.dialog.singlechoose.SingleChoiceDialogFragment
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.modelview.SingleTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListImageModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListWebLinkModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.ui.home.HomeState
import com.dinhlam.sharebox.ui.list.modelview.ShareListDateModelView
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
            withViewType(R.layout.model_view_single_text) {
                SingleTextModelView.SingleTextViewHolder(this)
            }

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
                }, ::openShareOptionClick)
            }

            withViewType(R.layout.model_view_share_list_web_link) {
                ShareListWebLinkModelView.ShareListWebLinkWebHolder(
                    this, ::openShareWeb, ::openShareOptionClick
                )
            }

            withViewType(R.layout.model_view_share_list_image) {
                ShareListImageModelView.ShareListImageViewHolder(
                    this, ::openShareOptionClick, ::viewImage
                )
            }
        }
    }

    private fun openShareOptionClick(shareId: Int) {
        val shareData = getState(viewModel) { state ->
            state.shareList.firstOrNull { share ->
                share.id == shareId
            }
        } ?: return

        val items = mutableMapOf<String, () -> Unit>()
        val icons = mutableListOf<Int>()

        items[getString(R.string.share_to_other)] = {
            shareHelper.shareToOther(shareData)
        }
        icons.add(R.drawable.ic_share)

        items[getString(R.string.delete)] = {
            viewModel.deleteShare(shareData)
        }
        icons.add(R.drawable.ic_delete_primary)

        BaseBottomSheetDialogFragment.showDialog(
            SingleChoiceDialogFragment::class, supportFragmentManager
        ) {
            arguments = Bundle().apply {
                putStringArray(
                    SingleChoiceDialogFragment.EXTRA_ITEM, items.keys.toTypedArray()
                )
                putIntArray(SingleChoiceDialogFragment.EXTRA_ICON, icons.toIntArray())
            }
            listener = SingleChoiceDialogFragment.OnItemSelectedListener { position ->
                val key = items.keys.toList()[position]
                items[key]?.invoke()
            }
        }
    }

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var appSharePref: AppSharePref

    override fun onCreateViewBinding(): ActivityShareListBinding {
        return ActivityShareListBinding.inflate(layoutInflater)
    }

    override val viewModel: ShareListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressHandler {
            val resultCode = getState(viewModel) { state -> state.resultCode }
            setResult(resultCode)
            finish()
        }

        viewBinding.recyclerView.adapter = shareListAdapter

        val folderId = intent.getStringExtra(ExtraUtils.EXTRA_FOLDER_ID) ?: ""
        viewModel.setFolderId(folderId)

        val searchQuery = intent.getStringExtra(SearchManager.QUERY) ?: ""
        searchQuery.takeIfNotNullOrBlank()?.let { nonNullQuery ->
            viewModel.setSearchQuery(nonNullQuery)
        }

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadShareList()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.consume(this, ShareListState::folderName) { folderName ->
            if (folderName.isNotBlank()) {
                supportActionBar?.title = folderName
            }
        }

        viewModel.consume(this, ShareListState::searchQuery) { query ->
            if (query.isNotBlank()) {
                supportActionBar?.title = getString(R.string.title_share_list_search, query)
            }
        }

        viewModel.consume(this, HomeState::toastRes) { toastRes ->
            if (toastRes != 0) {
                showToast(getString(toastRes))
                viewModel.clearToast()
            }
        }
    }

    override fun onStateChanged(state: ShareListState) {
        shareListAdapter.requestBuildModelViews()
    }

    private fun openShareWeb(url: String) {
        if (appSharePref.isCustomTabEnabled()) {
            appRouter.moveToChromeCustomTab(this, url)
        } else {
            appRouter.moveToBrowser(url)
        }
    }

    private fun viewImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "image/*")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        startActivity(intent)
    }
}
