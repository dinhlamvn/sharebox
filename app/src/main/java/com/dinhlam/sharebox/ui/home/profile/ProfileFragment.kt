package com.dinhlam.sharebox.ui.home.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.databinding.FragmentProfileBinding
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.modelview.DividerModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.profile.ProfileInfoModelView
import com.dinhlam.sharebox.modelview.profile.ProfileMenuItemModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListImageModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListImagesModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListTextModelView
import com.dinhlam.sharebox.modelview.sharelist.ShareListUrlModelView
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.viewholder.LoadingViewHolder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment :
    BaseViewModelFragment<ProfileState, ProfileViewModel, FragmentProfileBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = { false }) {
            viewModel.loadMores()
        }
    }

    private val adapter = BaseListAdapter.createAdapter({
        getState(viewModel) { state ->
            val nonNullUser = state.activeUser ?: return@getState run {
                add(LoadingModelView("loading_user"))
            }

            add(
                ProfileInfoModelView(
                    nonNullUser.id,
                    nonNullUser.avatar,
                    nonNullUser.name,
                    nonNullUser.drama,
                    nonNullUser.level,
                    nonNullUser.createdAt
                )
            )
            add(DividerModelView("divider", color = android.R.color.transparent, size = 16))
            add(DividerModelView("divider1", color = R.color.colorDividerLightV2))

            if (state.isRefreshing) {
                add(LoadingModelView("loading_profile"))
                return@getState
            }

            val models = state.shares.mapNotNull { shareDetail ->
                shareDetail.shareData.buildShareModelViews(
                    requireContext(),
                    shareDetail.shareId,
                    shareDetail.createdAt,
                    shareDetail.shareNote,
                    shareDetail.user
                )
            }
            if (models.isNotEmpty()) {
                models.forEachIndexed { idx, model ->
                    add(model)
                    add(DividerModelView("divider_$idx", size = 8))
                }

                if (state.isLoadMore) {
                    add(LoadingModelView("profile_load_more"))
                }
            }
        }
    }) {
        withViewType(R.layout.model_view_loading) {
            LoadingViewHolder(this)
        }

        withViewType(R.layout.model_view_profile_info) {
            ProfileInfoModelView.UserInfoViewHolder(this)
        }

        withViewType(R.layout.model_view_profile_menu_item) {
            ProfileMenuItemModelView.ProfileMenuItemViewHolder(this) {

            }
        }

        withViewType(R.layout.model_view_divider) {
            DividerModelView.DividerViewHolder(this)
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

        withViewType(R.layout.model_view_share_list_url) {
            ShareListUrlModelView.ShareListUrlWebHolder(this, {}, { })
        }

        withViewType(R.layout.model_view_share_list_image) {
            ShareListImageModelView.ShareListImageViewHolder(this, {}, {})
        }

        withViewType(R.layout.model_view_share_list_images) {
            ShareListImagesModelView.ShareListMultipleImageViewHolder(this, {}, {})
        }
    }

    override val viewModel: ProfileViewModel by viewModels()

    override fun onStateChanged(state: ProfileState) {
        adapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = adapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewBinding.swipeRefreshLayout.isRefreshing = false
            viewModel.doOnRefresh()
        }

        viewModel.consume(this, ProfileState::isLoadMore, true) { isLoadMore ->
            layoutManager.isLoadMore = isLoadMore
        }
    }
}