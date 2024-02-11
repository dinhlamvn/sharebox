package com.dinhlam.sharebox.ui.sharereceive

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
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
import com.dinhlam.sharebox.extensions.heightPercentage
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
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.modelview.ImageListModel
import com.dinhlam.sharebox.modelview.LoadingListModel
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.decoration.HorizontalCirclePagerItemDecoration
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.sharereceive.modelview.ShareReceiveTextListModel
import com.dinhlam.sharebox.ui.sharereceive.modelview.ShareReceiveUrlListModel
import com.dinhlam.sharebox.utils.Icons
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ShareReceiveActivity :
    BaseViewModelActivity<ShareReceiveState, ShareReceiveViewModel, ActivityShareReceiveBinding>(),
    BoxSelectionDialogFragment.OnBoxSelectedListener,
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener {

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

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }
    }

    private val passcodeConfirmResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                share()
            } else {
                showToast(R.string.error_require_passcode)
            }
        }

    @Inject
    lateinit var router: Router

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

    private val shareContentAdapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            when (val shareData = state.shareData) {
                is ShareData.ShareText -> add(
                    ShareReceiveTextListModel(
                        "shareText", shareData.castNonNull<ShareData.ShareText>().text
                    )
                )

                is ShareData.ShareUrl -> add(
                    ShareReceiveUrlListModel(
                        "shareWebLink", shareData.castNonNull<ShareData.ShareUrl>().url
                    )
                )

                is ShareData.ShareImage -> add(
                    ImageListModel(
                        shareData.castNonNull<ShareData.ShareImage>().uri,
                        screenHeight().times(0.5).toInt()
                    )
                )

                is ShareData.ShareImages -> {
                    addAll(shareData.castNonNull<ShareData.ShareImages>().uris.map { uri ->
                        ImageListModel(uri, height = screenHeight().times(0.5).toInt())
                    })
                }

                else -> add(LoadingListModel("loading"))
            }
        }
    }

    override fun onStateChanged(state: ShareReceiveState) {
        shareContentAdapter.requestBuildModelViews()
        updateUserInfo(state.activeUser)
    }

    private fun updateUserInfo(activeUser: UserDetail?) {
        activeUser?.let { user ->
            ImageLoader.INSTANCE.load(this, user.avatar, binding.imageAvatar) {
                copy(transformType = TransformType.Circle(ImageLoadScaleType.CenterCrop))
            }
            binding.textViewName.text = user.name
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressHandler {
            hideKeyboard()
            if (isTaskRoot) {
                finishAndRemoveTask()
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.container)
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior.halfExpandedRatio = 0.8f
        bottomSheetBehavior.peekHeight = heightPercentage(80)

        viewModel.consume(this, ShareReceiveState::showLoading) { isShow ->
            if (isShow) {
                binding.viewLoading.show()
            } else {
                binding.viewLoading.hide()
            }
        }

        binding.recyclerView.adapter = shareContentAdapter

        viewModel.consume(this, ShareReceiveState::isSaveSuccess) { isSaveSuccess ->
            if (isSaveSuccess) {
                Toast.makeText(this, R.string.shares_success, Toast.LENGTH_SHORT).show()
                if (isTaskRoot) {
                    finishAndRemoveTask()
                } else {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }

        binding.containerButtonShare.setOnClickListener {
            requestShare()
        }

        binding.imageShareBookmark.setOnClickListener {
            showBookmarkCollectionPicker()
        }

        viewModel.consume(this, ShareReceiveState::currentBox) { currentBox ->
            val boxName = currentBox?.boxName ?: getString(R.string.box_general)
            val isLock = currentBox?.passcode?.isNotBlank() ?: false
            binding.textShareBox.text = boxName
            binding.textShareBox.setDrawableCompat(
                start = Icons.boxIcon(this),
                end = if (isLock) Icons.lockIcon(this) { copy(sizeDp = 16) } else null,
            )
        }

        viewModel.consume(this, ShareReceiveState::bookmarkCollection) { collectionDetail ->
            collectionDetail?.let {
                binding.imageShareBookmark.setImageDrawable(Icons.bookmarkedIcon(this))
            } ?: binding.imageShareBookmark.setImageDrawable(Icons.bookmarkIcon(this))
        }

        binding.textShareBox.setOnClickListener {
            showPopupListShareBox()
        }

        if (!userHelper.isSignedIn()) {
            signInLauncher.launch(router.signIn(true))
        }

        binding.imageAddBox.setImageDrawable(Icons.addIcon(this))
        binding.imageAddBox.setOnClickListener {
            createBoxResultLauncher.launch(router.boxIntent(this))
        }

        binding.imageClose.setImageDrawable(Icons.closeIcon(this) {
            copy(sizeDp = 16)
        })
        binding.imageClose.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.textInputNote.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                activityScope.launch {
                    delay(700)
                    withContext(Dispatchers.Main) {
                        binding.scrollView.fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

        handleShareData()
    }

    private fun requestShare() = getState(viewModel) { state ->
        val boxPasscode =
            state.currentBox?.passcode.takeIfNotNullOrBlank() ?: return@getState share()
        val boxName = state.currentBox?.boxName ?: ""
        val intent = router.passcodeIntent(this, boxPasscode)
        intent.putExtra(
            AppExtras.EXTRA_PASSCODE_DESCRIPTION,
            getString(R.string.dialog_bookmark_collection_picker_verify_passcode, boxName)
        )
        passcodeConfirmResultLauncher.launch(intent)
    }

    private fun share() {
        hideKeyboard()
        val shareNote = binding.textInputNote.getTrimmedText()
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
        val images =
            intent.getParcelableArrayListExtraCompat<Parcelable>(Intent.EXTRA_STREAM) ?: return

        if (images.size > AppConsts.LIMIT_IMAGE_SHARE) {
            showToast(R.string.limit_share_image)
        }

        val takenImages = images.mapNotNull { it.cast<Uri>() }.take(AppConsts.LIMIT_IMAGE_SHARE)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        PagerSnapHelper().attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.addItemDecoration(
            HorizontalCirclePagerItemDecoration(
                colorActive = ContextCompat.getColor(this, R.color.colorPrimaryDark)
            )
        )
        viewModel.setShareData(ShareData.ShareImages(takenImages))
    }

    private fun openHome() {
        startActivity(
            router.home()
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
                    textView.setDrawableCompat(end = Icons.lockIcon(this@ShareReceiveActivity) {
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
            textView.text = getString(R.string.box_general)
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
            binding.containerShareBox,
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
            if (isTaskRoot) {
                finishAndRemoveTask()
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onBoxSelected(boxId: String) {
        viewModel.setBox(boxId)
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.setBookmarkCollection(bookmarkCollectionId)
    }
}

