package com.dinhlam.sharebox.ui.tags

import android.os.Bundle
import android.view.View
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityTagsBinding
import com.dinhlam.sharebox.dialog.optionmenu.BottomSheetOptionsMenuDialogFragment
import com.dinhlam.sharebox.dialog.tag.TagPickerDialogFragment
import com.dinhlam.sharebox.extensions.asColorInt
import com.dinhlam.sharebox.extensions.buildListItemListModel
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.dpF
import com.dinhlam.sharebox.extensions.getColorCompat
import com.dinhlam.sharebox.extensions.openShare
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.view.TagView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TagsActivity : BaseViewModelActivity<TagsState, TagsViewModel, ActivityTagsBinding>(),
    BottomSheetOptionsMenuDialogFragment.OnOptionItemSelectedListener {

    override fun onCreateViewBinding(): ActivityTagsBinding {
        return ActivityTagsBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var shareHelper: ShareHelper

    private val shareAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.asyncLoadShare is BaseViewModel.AsyncLoad.Loading) {
                return@getState LoadingListModel("loading").attachTo(this)
            }

            val shares = state.shares

            if (shares.isEmpty()) {
                return@getState TextListModel(
                    "text_empty", getString(R.string.no_result)
                ).attachTo(this)
            }

            state.shares.forEach { shareDetail ->
                shareDetail.buildListItemListModel(
                    ::showMore,
                    ::openShare
                ).attachTo(this)

                VerticalDividerListModel("divider_${shareDetail.shareId}").attachTo(this)
            }
        }
    }

    private fun openShare(shareDetail: ShareDetail) {
        openShare(supportFragmentManager, shareDetail, router, shareHelper)
    }

    private fun showMore(shareDetail: ShareDetail) {
        val arrayIcons = arrayOf(
            "f12d",
            "f061"
        )
        val choiceItems =
            resources.getStringArray(R.array.tags_option_menu_items)
                .mapIndexed { index, text ->
                    BottomSheetOptionsMenuDialogFragment.SingleChoiceItem(
                        arrayIcons[index], text
                    )
                }.toTypedArray()

        BottomSheetOptionsMenuDialogFragment.show(
            supportFragmentManager,
            choiceItems,
            bundleOf(AppExtras.EXTRA_SHARE_ID to shareDetail.shareId),
            this
        )
    }

    override val viewModel: TagsViewModel by viewModels()

    override fun onStateChanged(state: TagsState) {
        binding.tagView.isVisible = state.tagActive != null
        if (state.tagActive != null) {
            binding.tagView.setTagName(state.tagActive.tagName)
            binding.tagView.setTagColor(state.tagActive.tagColor.asColorInt())
        } else {
            binding.tagView.setTagName(null)
            binding.tagView.setTagColor(0)
        }
        shareAdapter.requestBuildListModels()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refresh()
        }

        binding.tagView.setOnClickListener { view ->
            showPopupTagPicker(view)
        }

        shareAdapter.attachTo(binding.recyclerView, this)
    }

    private fun showPopupTagPicker(view: View?) {
        val tags = getState(viewModel, TagsState::tags)

        if (tags.isEmpty()) {
            return
        }
        val popupWindow = PopupWindow(this)
        popupWindow.setBackgroundDrawable(null)

        val popupView = MaterialCardView(this, null, R.style.AppMaterialCardView)
        popupView.cardElevation = 8.dpF
        popupView.strokeWidth = 1.dp
        popupView.strokeColor = getColorCompat(R.color.grey_200)

        val cardViewContainer = LinearLayoutCompat(this)
        cardViewContainer.orientation = LinearLayoutCompat.VERTICAL
        cardViewContainer.setPadding(8.dp)
        tags.forEach { tag ->
            val tagView = TagView(this).apply {
                setTagName(tag.tagName)
                setTagColor(tag.tagColor.asColorInt())
            }

            tagView.setOnClickListener {
                viewModel.setActiveTag(tag)
                popupWindow.dismiss()
            }

            cardViewContainer.addView(
                tagView,
                LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 4.dp
                }
            )
        }

        popupView.addView(cardViewContainer)
        popupWindow.contentView = popupView
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(view, 0, 8.dp)
    }

    override fun onOptionItemSelected(position: Int, item: String, args: Bundle) {
        val shareId = args.getString(AppExtras.EXTRA_SHARE_ID) ?: return
        when (position) {
            0 -> viewModel.clearTag(shareId)
            1 -> TagPickerDialogFragment.showDialog(supportFragmentManager, shareId).apply {
                onDialogDismissListener = BaseDialogFragment.OnDialogDismissListener {
                    this@TagsActivity.viewModel.refresh()
                }
            }
        }
    }
}