package com.dinhlam.sharebox.ui.home.community

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.databinding.FragmentCommunityBinding
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.modelview.DividerModelView
import com.dinhlam.sharebox.modelview.FolderListModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.ShareRecentlyTitleModelView
import com.dinhlam.sharebox.modelview.SingleTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListImageModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListWebLinkModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.viewholder.LoadingViewHolder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommunityFragment :
    BaseViewModelFragment<CommunityState, CommunityViewModel, FragmentCommunityBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCommunityBinding {
        return FragmentCommunityBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var appSharePref: AppSharePref

    private val shareAdapter = BaseListAdapter.createAdapter({
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView)
                return@getState
            }

            val models = state.shareModelViews
            if (models.isEmpty()) {
                add(
                    SingleTextModelView(
                        getString(R.string.recently_empty), ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            } else {
                models.forEachIndexed { idx, model ->
                    add(model)
                    add(DividerModelView("divider_$idx", size = 8))
                }
            }
        }
    }) {
        withViewType(R.layout.model_view_loading) {
            LoadingViewHolder(this)
        }

        withViewType(R.layout.model_view_divider) {
            DividerModelView.DividerViewHolder(this)
        }

        withViewType(R.layout.model_view_share_recently_title) {
            ShareRecentlyTitleModelView.ShareRecentlyTitleViewHolder(this)
        }

        withViewType(R.layout.model_view_single_text) {
            SingleTextModelView.SingleTextViewHolder(this)
        }

        withViewType(R.layout.model_view_folder_list) {
            FolderListModelView.FolderListViewHolder(
                this, {

                }, {

                }
            )
        }

        withViewType(R.layout.model_view_share_list_text) {
            ShareListTextModelView.ShareListTextViewHolder(
                this,
                { textContent ->
                    val dialog = TextViewerDialogFragment()
                    dialog.arguments = Bundle().apply {
                        putString(Intent.EXTRA_TEXT, textContent)
                    }
                    dialog.show(parentFragmentManager, "TextViewerDialogFragment")
                },
            ) {

            }
        }

        withViewType(R.layout.model_view_share_list_web_link) {
            ShareListWebLinkModelView.ShareListWebLinkWebHolder(
                this, ::openWebLink, ::shareToOther
            )
        }

        withViewType(R.layout.model_view_share_list_image) {
            ShareListImageModelView.ShareListImageViewHolder(this, ::shareToOther, ::viewImage)
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    override val viewModel: CommunityViewModel by viewModels()

    override fun onStateChanged(state: CommunityState) {
        shareAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.recyclerView.adapter = shareAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadShares()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun openWebLink(url: String) {
        if (appSharePref.isCustomTabEnabled()) {
            appRouter.moveToChromeCustomTab(requireContext(), url)
        } else {
            appRouter.moveToBrowser(url)
        }
    }

    private fun shareToOther(shareId: Int) = getState(viewModel) { state ->
        val share = state.shareList.firstOrNull { it.id == shareId } ?: return@getState
        shareHelper.shareToOther(share)
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