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
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseSpanSizeLookup
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.ShareMode
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.databinding.ActivityShareReceiveBinding
import com.dinhlam.sharebox.databinding.MenuItemIconWithTextSubtextBinding
import com.dinhlam.sharebox.databinding.ModelViewHashtagBinding
import com.dinhlam.sharebox.databinding.ModelViewLoadingBinding
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveImageBinding
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveImagesBinding
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveTextBinding
import com.dinhlam.sharebox.databinding.ModelViewShareReceiveUrlBinding
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.dpF
import com.dinhlam.sharebox.extensions.getParcelableArrayListExtraCompat
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.screenWidth
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.setNonBlankText
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.modelview.HashTagModelView
import com.dinhlam.sharebox.modelview.LoadingModelView
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.ui.sharereceive.modelview.ShareReceiveImageModelView
import com.dinhlam.sharebox.ui.sharereceive.modelview.ShareReceiveImagesModelView
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

    private val hashtagAdapter = BaseListAdapter.createAdapter({
        getState(viewModel) { state ->
            if (state.hashTags.isNotEmpty()) {
                addAll(state.hashTags.map { ht ->
                    HashTagModelView(ht.hashTagId, ht.hashTagName)
                })
            } else {
                add(HashTagModelView(HASHTAG_DEFAULT_ID, "+"))
            }
        }
    }) {
        withViewType(R.layout.model_view_hashtag) {
            HashTagModelView.HashTagViewHolder(ModelViewHashtagBinding.bind(this)) { hashTagId ->
                if (hashTagId == HASHTAG_DEFAULT_ID) {
                    showToast("ADD NEW HASHTAG")
                }
            }
        }
    }

    private val shareContentAdapter = BaseListAdapter.createAdapter({

        fun getSpanSize(size: Int, index: Int): Int {
            return when (size) {
                AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT -> if (index < 2) 3 else 2
                else -> 1
            }
        }

        fun getImageWidth(size: Int, index: Int): Int {
            val screenWidth = screenWidth()

            return when (size) {
                4 -> screenWidth.div(2)
                AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT -> if (index < 2) screenWidth.div(2) else screenWidth.div(
                    3
                )

                else -> screenWidth.div(size)
            }
        }

        fun getTextNumber(realSize: Int, takeSize: Int, index: Int): String {
            return when {
                realSize > takeSize && index == takeSize - 1 -> "+${realSize - takeSize}"
                else -> ""
            }
        }

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
                    ShareReceiveImageModelView(
                        "shareImage", shareData.uri
                    )
                )

                is ShareData.ShareImages -> {
                    val pickItems = shareData.uris.take(AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT)

                    addAll(pickItems.mapIndexed { index, uri ->
                        ShareReceiveImagesModelView(
                            "shareMultipleImage$index",
                            uri,
                            getSpanSize(pickItems.size, index),
                            getImageWidth(pickItems.size, index),
                            getTextNumber(shareData.uris.size, pickItems.size, index)
                        )
                    })

                }

                else -> add(LoadingModelView("loading"))
            }
        }
    }) {
        withViewType(R.layout.model_view_loading) {
            LoadingModelView.LoadingViewHolder(ModelViewLoadingBinding.bind(this))
        }

        withViewType(R.layout.model_view_share_receive_text) {
            ShareReceiveTextModelView.ShareTextViewHolder(ModelViewShareReceiveTextBinding.bind(this))
        }

        withViewType(R.layout.model_view_share_receive_image) {
            ShareReceiveImageModelView.ShareImageViewHolder(
                ModelViewShareReceiveImageBinding.bind(this)
            )
        }

        withViewType(R.layout.model_view_share_receive_images) {
            ShareReceiveImagesModelView.ShareReceiveImagesViewHolder(
                ModelViewShareReceiveImagesBinding.bind(this)
            )
        }

        withViewType(R.layout.model_view_share_receive_url) {
            ShareReceiveUrlModelView.ShareReceiveUrlViewHolder(
                ModelViewShareReceiveUrlBinding.bind(this)
            )
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

        viewBinding.recyclerView.adapter = shareContentAdapter

        viewModel.consume(this, ShareReceiveState::isSaveSuccess) { isSaveSuccess ->
            if (isSaveSuccess) {
                Toast.makeText(this, R.string.save_share_successfully, Toast.LENGTH_SHORT).show()
                finishAndRemoveTask()
            }
        }

        viewBinding.buttonPost.setOnClickListener {
            hideKeyboard()
            val shareNote = viewBinding.textInputNote.getTrimmedText()
            viewModel.share(shareNote, this@ShareReceiveActivity)
        }

        viewModel.consume(this, ShareReceiveState::shareMode) { shareMode ->
            viewBinding.textShareMode.setDrawableCompat(start = shareMode.icon)
            viewBinding.textShareMode.setNonBlankText(getString(shareMode.text))
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
                handleShareMultipleImage(intent)
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

    private fun handleShareMultipleImage(intent: Intent) {
        intent.getParcelableArrayListExtraCompat<Parcelable>(Intent.EXTRA_STREAM)?.let { list ->
            val data = list.mapNotNull { it.cast<Uri>() }
            val spanCount = when {
                data.size == 4 -> 2
                data.size > AppConsts.SHARE_IMAGES_PICK_ITEM_LIMIT -> 6
                else -> data.size
            }
            viewBinding.recyclerView.layoutManager = GridLayoutManager(this, spanCount).apply {
                spanSizeLookup = BaseSpanSizeLookup(shareContentAdapter, spanCount)
            }

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
        popupWindow.elevation = 10.dpF(this)
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
        popupWindow.showAsDropDown(viewBinding.containerShareMode)
    }
}
