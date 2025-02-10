package com.dinhlam.sharebox.ui.sharereceive

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityShareReceiveBinding
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.getFileNameAndSize
import com.dinhlam.sharebox.extensions.getParcelableArrayListExtraCompat
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.hideKeyboard
import com.dinhlam.sharebox.extensions.isWebLink
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.extensions.screenHeight
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.imageloader.config.TransformType
import com.dinhlam.sharebox.imageloader.load
import com.dinhlam.sharebox.listmodel.ImageListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.UserDetail
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.recyclerview.decoration.HorizontalCirclePagerItemDecoration
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.sharereceive.listmodel.ShareReceiveFileListModel
import com.dinhlam.sharebox.ui.sharereceive.listmodel.ShareReceiveTextListModel
import com.dinhlam.sharebox.ui.sharereceive.listmodel.ShareReceiveUrlListModel
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ShareReceiveActivity :
    BaseViewModelActivity<ShareReceiveState, ShareReceiveViewModel, ActivityShareReceiveBinding>(),
    BookmarkCollectionPickerDialogFragment.OnBookmarkCollectionPickListener {

    private val chooseBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                viewModel.setBox(boxId)
            }
        }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { boxId ->
                    viewModel.setBox(boxId)
                }
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

    private val shareContentAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            when (val shareData = state.shareData) {
                is ShareData.ShareText -> ShareReceiveTextListModel(
                    "shareText", shareData.text
                ).attachTo(this)

                is ShareData.ShareUrl -> ShareReceiveUrlListModel(
                    "shareWebLink", shareData.url
                ).attachTo(this)

                is ShareData.ShareImage -> ImageListModel(
                    shareData.uri,
                    screenHeight().times(0.5).toInt()
                ).attachTo(this)

                is ShareData.ShareImages -> {
                    shareData.uris.map { uri ->
                        ImageListModel(uri, height = screenHeight().times(0.5).toInt()).attachTo(
                            this
                        )
                    }
                }

                is ShareData.ShareFile -> ShareReceiveFileListModel(
                    "share_file",
                    shareData.fileName,
                    shareData.fileSize,
                ).attachTo(this)

                else -> LoadingListModel("loading").attachTo(this)
            }
        }
    }

    override fun onStateChanged(state: ShareReceiveState) {
        shareContentAdapter.requestBuildListModels()
        updateUserInfo(state.activeUser)
    }

    private fun updateUserInfo(activeUser: UserDetail?) {
        activeUser?.let { user ->
            binding.imageAvatar.load(this, user.avatar) {
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

        shareContentAdapter.attachTo(binding.recyclerView, this)

        binding.containerButtonShare.setOnClickListener {
            share()
        }

        binding.imageShareBookmark.setOnClickListener {
            showBookmarkCollectionPicker()
        }

        binding.textShareBox.setOnClickListener {
            chooseBoxLauncher.launch(router.boxList(this, null))
        }

        binding.imageAddBox.setImageDrawable(Icons.addIcon(this))
        binding.imageAddBox.setOnClickListener {
            createBoxResultLauncher.launch(router.boxForm(this, null))
        }

        binding.imageClose.setImageDrawable(Icons.closeIcon(this) {
            copy(sizeDp = 16)
        })
        binding.imageClose.setOnClickListener {
            finishAndRemoveTask()
        }

        binding.textInputNote.setOnFocusChangeListener { v, _ ->
            scrollToBottomEditText(v)
        }

        binding.textInputNote.setOnClickListener { v ->
            scrollToBottomEditText(v)
        }

        onChange(ShareReceiveState::currentBox) { currentBox ->
            val boxName = currentBox?.boxName
            val isLock = currentBox?.passcode?.isNotBlank() ?: false
            binding.textShareBox.text = boxName
            binding.textShareBox.setDrawableCompat(
                start = Icons.boxIcon(this),
                end = if (isLock) Icons.lockIcon(this) { copy(sizeDp = 16) } else null,
            )
        }

        onChange(ShareReceiveState::bookmarkCollection) { collectionDetail ->
            collectionDetail?.let {
                binding.imageShareBookmark.setImageDrawable(Icons.bookmarkedIcon(this))
            } ?: binding.imageShareBookmark.setImageDrawable(Icons.bookmarkIcon(this))
        }

        onChange(ShareReceiveState::asyncLoadArchive) { asyncLoad ->
            if (asyncLoad is BaseViewModel.AsyncLoad.Loading) {
                binding.viewLoading.show()
            } else {
                binding.viewLoading.hide()
            }

            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                Toast.makeText(this, R.string.shares_success, Toast.LENGTH_SHORT).show()
                if (isTaskRoot) {
                    finishAndRemoveTask()
                } else {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } else if (asyncLoad is BaseViewModel.AsyncLoad.Failed) {
                showToast(asyncLoad.error.message)
            }
        }

        viewModel.getCurrentUserProfile()

        handleShareData()
    }

    private fun scrollToBottomEditText(v: View) {
        if (v.hasFocus()) {
            activityScope.launch {
                delay(500)
                withContext(Dispatchers.Main) {
                    binding.scrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    private fun share() = getState(viewModel) { state ->
        hideKeyboard()

        if (state.currentBox == null) {
            showToast(R.string.require_choose_box)
            binding.containerShareBox.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.zoom_in
                )
            )
            return@getState
        }

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

            action == Intent.ACTION_SEND -> {
                handleShareFile(intent)
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
                colorActive = ContextCompat.getColor(this, R.color.md_theme_onPrimary)
            )
        )
        viewModel.setShareData(ShareData.ShareImages(takenImages))
    }

    private fun handleShareFile(intent: Intent) {
        intent.getParcelableExtraCompat<Parcelable>(Intent.EXTRA_STREAM).cast<Uri>()
            ?.let { shareUri ->
                val fileInfo = getFileNameAndSize(shareUri)
                if (fileInfo.second > 10 * 1024 * 1024) {
                    showToast(R.string.reach_limit_file_size)
                    return@let finishAndRemoveTask()
                }
                viewModel.setShareData(
                    ShareData.ShareFile(
                        fileInfo.first,
                        fileInfo.second,
                        shareUri
                    )
                )
            }
    }

    private fun openHome() {
        startActivity(
            router.home()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun showBookmarkCollectionPicker() = getState(viewModel) { state ->
        shareHelper.showBookmarkCollectionPickerDialog(
            supportFragmentManager, "", state.bookmarkCollection?.id
        )
    }

    override fun onBookmarkCollectionDone(shareId: String, bookmarkCollectionId: String?) {
        viewModel.setBookmarkCollection(bookmarkCollectionId)
    }
}

