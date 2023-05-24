package com.dinhlam.sharebox.ui.sharereceive

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.databinding.ActivityShareReceiveBinding
import com.dinhlam.sharebox.databinding.MenuItemIconWithTextSubtextBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.dpF
import com.dinhlam.sharebox.extensions.getParcelableArrayListExtraCompat
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.modelview.HashTagModelView
import com.dinhlam.sharebox.modelview.ImageModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.decoration.HorizontalCirclePagerItemDecoration
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.ui.sharereceive.modelview.ShareReceiveTextModelView
import com.dinhlam.sharebox.ui.sharereceive.modelview.ShareReceiveUrlModelView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareReceiveActivity :
    BaseViewModelActivity<ShareReceiveState, ShareReceiveViewModel, ActivityShareReceiveBinding>() {

    companion object {
        private const val HASHTAG_DEFAULT_ID = "hashtag-default"
    }

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appPref: AppSharePref

    override fun onCreateViewBinding(): ActivityShareReceiveBinding {
        return ActivityShareReceiveBinding.inflate(layoutInflater)
    }

    override val viewModel: ShareReceiveViewModel by viewModels()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareData()
    }

    private val hashtagAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.hashTags.isNotEmpty()) {
                addAll(state.hashTags.map { ht ->
                    HashTagModelView(ht.hashTagId, ht.hashTagName)
                })
            } else {
                add(HashTagModelView(HASHTAG_DEFAULT_ID, "+"))
            }
        }
    }

    private val shareContentAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            when (val shareData = state.shareData) {
                is ShareData.ShareText -> add(
                    ShareReceiveTextModelView(
                        "shareText", shareData.text
                    )
                )

                is ShareData.ShareUrl -> add(
                    ShareReceiveUrlModelView(
                        "shareWebLink", shareData.url
                    )
                )

                is ShareData.ShareImage -> add(
                    ImageModelView(shareData.uri, screenHeight().times(0.5).toInt())
                )

                is ShareData.ShareImages -> {
                    addAll(shareData.uris.map { uri ->
                        ImageModelView(uri, height = screenHeight().times(0.5).toInt())
                    })
                }

                else -> add(LoadingModelView("loading"))
            }
        }
    }

    override fun onStateChanged(state: ShareReceiveState) {
        shareContentAdapter.requestBuildModelViews()
        hashtagAdapter.requestBuildModelViews()
        invalidateUserInfo(state.activeUser)
    }

    private fun invalidateUserInfo(activeUser: UserDetail?) {
        activeUser?.let { user ->
            ImageLoader.instance.load(this, user.avatar, viewBinding.imageAvatar) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }
            viewBinding.textViewName.text = user.name
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressHandler {
            hideKeyboard()
            finishAndRemoveTask()
        }

        viewModel.consume(this, ShareReceiveState::showLoading, true) { isShow ->
            if (isShow) {
                viewBinding.viewLoading.show()
            } else {
                viewBinding.viewLoading.hide()
            }
        }

        viewBinding.recyclerView.adapter = shareContentAdapter

        viewModel.consume(this, ShareReceiveState::isSaveSuccess) { isSaveSuccess ->
            if (isSaveSuccess) {
                Toast.makeText(this, R.string.save_share_successfully, Toast.LENGTH_SHORT).show()
                finishAndRemoveTask()
            }
        }

        viewBinding.containerButtonPost.setOnClickListener {
            hideKeyboard()
            val shareNote = viewBinding.textInputNote.getTrimmedText()
            viewModel.share(shareNote, this@ShareReceiveActivity)
        }

        viewBinding.containerShareBookmark.setOnClickListener {
            showBookmarkCollectionPicker()
        }

        viewModel.consume(this, ShareReceiveState::shareMode) { shareMode ->
            viewBinding.imageShareMode.setImageResource(shareMode.icon)
        }

        viewModel.consume(this, ShareReceiveState::bookmarkCollection) { collectionDetail ->
            collectionDetail?.let { collection ->
                viewBinding.textShareBookmark.setTextColor(
                    ContextCompat.getColor(
                        this, R.color.primaryColor
                    )
                )
                viewBinding.textShareBookmark.text = collection.name
                viewBinding.textShareBookmark.setDrawableCompat(start = R.drawable.ic_bookmarked)
            } ?: viewBinding.textShareBookmark.apply {
                setTextColor(
                    ContextCompat.getColor(
                        this@ShareReceiveActivity,
                        R.color.colorHint
                    )
                )
                setText(R.string.share_receive_no_bookmark)
                viewBinding.textShareBookmark.setDrawableCompat(start = R.drawable.ic_bookmark)
            }
        }

        viewBinding.containerShareMode.setOnClickListener {
            showPopupChooseShareMode()
        }

        handleShareData()
    }

    private fun handleShareData() {
        val (action, type) = intent.action to intent.type
        when {
            action == Intent.ACTION_SEND && type?.startsWith("text/") == true -> {
                val shareContent = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                handleShareText(shareContent)
            }

            action == Intent.ACTION_SEND && type?.startsWith("image/") == true -> {
                handleShareImage(intent)
            }

            action == Intent.ACTION_SEND_MULTIPLE && type?.startsWith("image/") == true -> {
                handleShareImages(intent)
            }

            action == Intent.ACTION_PROCESS_TEXT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                handleProcessText(intent)
            }

            else -> openHome()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleProcessText(intent: Intent) {
        val isReadOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
        if (isReadOnly) {
            val content = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""
            val nonNullContent = content.toString().takeIfNotNullOrBlank() ?: return openHome()
            handleShareText(nonNullContent)
        }
    }

    private fun handleShareText(text: String) {
        val shareInfo = when {
            text.isWebLink() -> ShareData.ShareUrl(text)
            else -> ShareData.ShareText(text)
        }
        viewModel.setShareInfo(shareInfo)
    }

    private fun handleShareImage(intent: Intent) {
        intent.getParcelableExtraCompat<Parcelable>(Intent.EXTRA_STREAM).cast<Uri>()
            ?.let { shareUri ->
                viewModel.setShareInfo(ShareData.ShareImage(shareUri))
            }
    }

    private fun handleShareImages(intent: Intent) {
        intent.getParcelableArrayListExtraCompat<Parcelable>(Intent.EXTRA_STREAM)?.let { list ->
            val data = list.mapNotNull { it.cast<Uri>() }
            viewBinding.recyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            PagerSnapHelper().attachToRecyclerView(viewBinding.recyclerView)
            viewBinding.recyclerView.addItemDecoration(
                HorizontalCirclePagerItemDecoration(
                    colorActive = ContextCompat.getColor(this, R.color.primaryDarkColor)
                )
            )
            viewModel.setShareInfo(ShareData.ShareImages(data))
        }
    }

    private fun openHome() {
        startActivity(
            appRouter.home()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun showPopupChooseShareMode() {
        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(this)
        popupWindow.width = width
        popupWindow.height = height
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 10.dpF()
        popupWindow.isOutsideTouchable = true

        fun dismissPopup() {
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        }

        val popupView = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(
            width, height
        )
        popupView.orientation = LinearLayout.VERTICAL
        popupView.layoutParams = layoutParams

        arrayOf(ShareMode.ShareModeCommunity, ShareMode.ShareModePersonal).forEach { shareMode ->
            val binding = MenuItemIconWithTextSubtextBinding.inflate(layoutInflater)
            binding.imageIcon.setImageResource(shareMode.icon)
            binding.textTitle.text = getString(shareMode.text)
            binding.textSubtext.text = getString(shareMode.subText)
            binding.root.setOnClickListener {
                viewModel.setShareMode(shareMode)
                dismissPopup()
            }
            popupView.addView(binding.root, layoutParams)
        }
        popupWindow.contentView = popupView

        popupWindow.showAsDropDown(viewBinding.containerShareMode, 0, -1 * (70 * 2).dp())
    }

    private fun showBookmarkCollectionPicker() = getState(viewModel) { state ->
        shareHelper.showBookmarkCollectionPicker(this, state.bookmarkCollection?.id) { pickedId ->
            viewModel.setBookmarkCollection(pickedId)
        }
    }
}
