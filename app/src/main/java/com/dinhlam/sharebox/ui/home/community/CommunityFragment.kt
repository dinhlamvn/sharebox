package com.dinhlam.sharebox.ui.home.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.databinding.FragmentCommunityBinding
import com.dinhlam.sharebox.extensions.buildShareModelViews
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.widthPercentage
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.LoadMoreLinearLayoutManager
import com.dinhlam.sharebox.utils.BoxUtils
import com.dinhlam.sharebox.utils.IconUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommunityFragment :
    BaseViewModelFragment<CommunityState, CommunityViewModel, FragmentCommunityBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentCommunityBinding {
        return FragmentCommunityBinding.inflate(inflater, container, false)
    }

    private val layoutManager by lazy {
        LoadMoreLinearLayoutManager(requireContext(), blockShouldLoadMore = {
            return@LoadMoreLinearLayoutManager getState(viewModel) { state -> state.canLoadMore && !state.isLoadingMore }
        }) {
            viewModel.loadMores()
        }
    }

    private val shareAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                add(LoadingModelView("top_loading"))
            }

            if (state.shares.isEmpty() && !state.isRefreshing) {
                add(
                    TextModelView(
                        "text_empty", getString(R.string.no_result)
                    )
                )
            } else if (state.shares.isNotEmpty()) {
                val models = state.shares.map { shareDetail ->
                    shareDetail.shareData.buildShareModelViews(
                        screenHeight(),
                        shareDetail.shareId,
                        shareDetail.shareDate,
                        shareDetail.shareNote,
                        shareDetail.user,
                        shareDetail.likeNumber,
                        commentNumber = shareDetail.commentNumber,
                        bookmarked = shareDetail.bookmarked,
                        liked = shareDetail.liked,
                        actionOpen = ::onOpen,
                        actionShareToOther = ::onShareToOther,
                        actionLike = ::onLike,
                        actionComment = ::onComment,
                        actionBookmark = ::onBookmark
                    )
                }
                models.forEach { model ->
                    add(model)
                    add(
                        SizedBoxModelView(
                            "divider_${model.modelId}",
                            height = 8.dp(),
                            backgroundColor = R.color.colorDividerLightV2
                        )
                    )
                }

                if (state.isLoadingMore) {
                    add(LoadingModelView("home_load_more_${state.currentPage}"))
                }
            }
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    override val viewModel: CommunityViewModel by viewModels()

    override fun onStateChanged(state: CommunityState) {
        shareAdapter.requestBuildModelViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.textTitle.setOnClickListener(::showListBoxMenu)

        viewBinding.textTitle.setDrawableCompat(end = IconUtils.boxIcon(requireContext()))

        val box = BoxUtils.getBoxes().first()
        viewBinding.textTitle.text = getString(box.name)

        shareAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    layoutManager.scrollToPositionWithOffset(0, 0)
                }
            }
        })

        viewBinding.recyclerView.itemAnimator?.cast<DefaultItemAnimator>()?.supportsChangeAnimations =
            false
        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = shareAdapter

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.doOnRefresh()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.consume(this, CommunityState::isLoadingMore, true) { isLoadMore ->
            layoutManager.hadTriggerLoadMore = isLoadMore
        }
    }

    private fun showListBoxMenu(view: View) {
        val listPopupWindow = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)
        listPopupWindow.anchorView = view
        val boxes = BoxUtils.getBoxes().map { getString(it.name) }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_popup_window_item, boxes)
        listPopupWindow.setAdapter(adapter)
        listPopupWindow.setContentWidth(widthPercentage(50))

        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
            listPopupWindow.dismiss()
            showToast(boxes[position])
        }

        listPopupWindow.show()
    }

    private fun onOpen(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> {
                shareHelper.openUrl(
                    requireContext(), shareData.url, appSharePref.isCustomTabEnabled()
                )
            }

            is ShareData.ShareText -> {
                shareHelper.openTextViewer(requireActivity(), shareData.text)
            }

            is ShareData.ShareImage -> {
                shareHelper.viewShareImage(requireActivity(), shareData.uri)
            }

            is ShareData.ShareImages -> {
                shareHelper.viewShareImages(requireActivity(), shareData.uris)
            }
        }
    }

    private fun onShareToOther(shareId: String) = getState(viewModel) { state ->
        val share =
            state.shares.firstOrNull { share -> share.shareId == shareId } ?: return@getState
        shareHelper.shareToOther(share)
    }

    private fun onLike(shareId: String) {
        viewModel.like(shareId)
    }

    private fun onBookmark(shareId: String) {
        viewModel.showBookmarkCollectionPicker(shareId) { collectionId ->
            shareHelper.showBookmarkCollectionPicker(requireActivity(), collectionId) { pickedId ->
                viewModel.bookmark(shareId, pickedId)
            }
        }
    }

    private fun onComment(shareId: String) {
        shareHelper.showComment(childFragmentManager, shareId)
    }
}