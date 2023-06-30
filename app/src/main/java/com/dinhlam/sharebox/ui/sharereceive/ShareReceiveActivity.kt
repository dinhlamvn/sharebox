package com.dinhlam.sharebox.ui.sharereceive

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.model.ShareData
import com.dinhlam.sharebox.data.model.UserDetail
import com.dinhlam.sharebox.databinding.ActivityShareReceiveBinding
import com.dinhlam.sharebox.databinding.MenuItemWithTextBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.castNonNull
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
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotEmpty
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
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
import com.dinhlam.sharebox.utils.IconUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareReceiveActivity :
    BaseViewModelActivity<ShareReceiveState, ShareReceiveViewModel, ActivityShareReceiveBinding>(),
    BoxSelectionDialogFragment.OnBoxSelectedListener,
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener {

    companion object {
        private const val HASHTAG_DEFAULT_ID = "hashtag-default"
    }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { boxId ->
                    viewModel.setBox(boxId)
                    viewModel.loadBoxes()
                }
            }
        }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleSignInResult
    )

    private val passcodeConfirmResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                snap()
            } else {
                showToast(R.string.error_require_passcode)
            }
        }

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appPref: AppSharePref

    @Inject
    lateinit var userHelper: UserHelper

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
                        "shareText", shareData.castNonNull<ShareData.ShareText>().text
                    )
                )

                is ShareData.ShareUrl -> add(
                    ShareReceiveUrlModelView(
                        "shareWebLink", shareData.castNonNull<ShareData.ShareUrl>().url
                    )
                )

                is ShareData.ShareImage -> add(
                    ImageModelView(
                        shareData.castNonNull<ShareData.ShareImage>().uri,
                        screenHeight().times(0.5).toInt()
                    )
                )

                is ShareData.ShareImages -> {
                    addAll(shareData.castNonNull<ShareData.ShareImages>().uris.map { uri ->
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
        updateUserInfo(state.activeUser)
    }

    private fun updateUserInfo(activeUser: UserDetail?) {
        activeUser?.let { user ->
            ImageLoader.INSTANCE.load(this, user.avatar, viewBinding.imageAvatar) {
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

        viewModel.consume(this, ShareReceiveState::showLoading) { isShow ->
            if (isShow) {
                viewBinding.viewLoading.show()
            } else {
                viewBinding.viewLoading.hide()
            }
        }

        viewBinding.recyclerView.adapter = shareContentAdapter

        viewModel.consume(this, ShareReceiveState::isSaveSuccess) { isSaveSuccess ->
            if (isSaveSuccess) {
                Toast.makeText(this, R.string.snap_success, Toast.LENGTH_SHORT).show()
                finishAndRemoveTask()
            }
        }

        viewBinding.containerButtonSnap.setOnClickListener {
            requestSnap()
        }

        viewBinding.imageShareBookmark.setOnClickListener {
            showBookmarkCollectionPicker()
        }

        viewModel.consume(this, ShareReceiveState::currentBox) { currentBox ->
            val boxName = currentBox?.boxName ?: getString(R.string.box_community)
            val isLock = currentBox?.passcode?.isNotBlank() ?: false
            viewBinding.textShareBox.text = boxName
            viewBinding.textShareBox.setDrawableCompat(
                start = IconUtils.boxIcon(this),
                end = if (isLock) IconUtils.lockIcon(this) { copy(sizeDp = 16) } else null,
            )
        }

        viewModel.consume(this, ShareReceiveState::bookmarkCollection) { collectionDetail ->
            collectionDetail?.let {
                viewBinding.imageShareBookmark.setImageDrawable(IconUtils.bookmarkedIcon(this))
            } ?: viewBinding.imageShareBookmark.setImageDrawable(IconUtils.bookmarkIcon(this))
        }

        viewBinding.textShareBox.setOnClickListener {
            showPopupListShareBox()
        }

        handleShareData()

        if (!userHelper.isSignedIn()) {
            signInLauncher.launch(appRouter.signIn(true))
        }

        viewBinding.imageAddBox.setImageDrawable(IconUtils.addIcon(this))
        viewBinding.imageAddBox.setOnClickListener {
            createBoxResultLauncher.launch(appRouter.boxIntent(this))
        }
    }

    private fun requestSnap() = getState(viewModel) { state ->
        val boxPasscode =
            state.currentBox?.passcode.takeIfNotNullOrBlank() ?: return@getState snap()
        val boxName = state.currentBox?.boxName ?: ""
        val intent = appRouter.passcodeIntent(this, boxPasscode)
        intent.putExtra(
            AppExtras.EXTRA_PASSCODE_DESCRIPTION,
            getString(R.string.dialog_bookmark_collection_picker_verify_passcode, boxName)
        )
        passcodeConfirmResultLauncher.launch(intent)
    }

    private fun snap() {
        hideKeyboard()
        val shareNote = viewBinding.textInputNote.getTrimmedText()
        viewModel.share(shareNote, this@ShareReceiveActivity)
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
        viewModel.setShareData(shareInfo)
    }

    private fun handleShareImage(intent: Intent) {
        intent.getParcelableExtraCompat<Parcelable>(Intent.EXTRA_STREAM).cast<Uri>()
            ?.let { shareUri ->
                viewModel.setShareData(ShareData.ShareImage(shareUri))
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
                    colorActive = ContextCompat.getColor(this, R.color.colorPrimaryDark)
                )
            )
            viewModel.setShareData(ShareData.ShareImages(data))
        }
    }

    private fun openHome() {
        startActivity(
            appRouter.home()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun showPopupListShareBox() = getState(viewModel) { state ->
        val boxes =
            state.boxes.takeIfNotEmpty()?.take(AppConsts.NUMBER_VISIBLE_BOX) ?: return@getState
        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(this, null, R.attr.listPopupWindowStyle)
        popupWindow.width = width
        popupWindow.height = height
        popupWindow.elevation = 10.dpF()
        popupWindow.isOutsideTouchable = true

        fun dismissPopup() {
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        }

        val popupView = ScrollView(this)
        popupView.isFillViewport = true
        popupView.layoutParams = ViewGroup.LayoutParams(width, height)

        val popupContentView = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(
            width, height
        )
        popupContentView.orientation = LinearLayout.VERTICAL
        popupContentView.layoutParams = layoutParams

        boxes.forEach { box ->
            MenuItemWithTextBinding.inflate(layoutInflater).apply {
                textView.text = box.boxName
                textView.setOnClickListener {
                    viewModel.setBox(box)
                    dismissPopup()
                }
                if (box.passcode?.isNotBlank() == true) {
                    textView.setDrawableCompat(end = IconUtils.lockIcon(this@ShareReceiveActivity) {
                        copy(
                            sizeDp = 16
                        )
                    })
                }
                popupContentView.addView(
                    this.root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
                )
            }
        }

        MenuItemWithTextBinding.inflate(layoutInflater).apply {
            textView.text = getString(R.string.box_community)
            textView.setOnClickListener {
                viewModel.setBox(null)
                dismissPopup()
            }
            popupContentView.addView(
                this.root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
            )
        }

        val hasViewMore = state.boxes.size > AppConsts.NUMBER_VISIBLE_BOX
        if (hasViewMore) {
            MenuItemWithTextBinding.inflate(layoutInflater).apply {
                textView.text = getString(R.string.view_more)
                textView.setOnClickListener {
                    shareHelper.showBoxSelectionDialog(supportFragmentManager)
                    dismissPopup()
                }
                popupContentView.addView(
                    this.root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
                )
            }
        }

        popupView.addView(popupContentView)
        popupWindow.contentView = popupView

        popupWindow.showAsDropDown(
            viewBinding.containerShareBox,
            0,
            boxes.size.plus(if (hasViewMore) 2 else 1).times(-1).times(60).dp()
        )
    }

    private fun showBookmarkCollectionPicker() = getState(viewModel) { state ->
        shareHelper.showBookmarkCollectionPickerDialog(
            supportFragmentManager, "", state.bookmarkCollection?.id
        )
    }

    private fun handleSignInResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            viewModel.getCurrentUserProfile()
        } else {
            showToast(R.string.sign_in_error)
            finishAndRemoveTask()
        }
    }

    override fun onBoxSelected(boxId: String) {
        viewModel.setBox(boxId)
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.setBookmarkCollection(bookmarkCollectionId)
    }
}

