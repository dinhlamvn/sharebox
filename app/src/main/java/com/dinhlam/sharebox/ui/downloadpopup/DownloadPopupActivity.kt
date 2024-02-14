package com.dinhlam.sharebox.ui.downloadpopup

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityDownloadPopupBinding
import com.dinhlam.sharebox.extensions.asFileExtension
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.getParcelableArrayListExtraCompat
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.helper.DownloadHelper
import com.dinhlam.sharebox.listmodel.SizedBoxListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.model.DownloadData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DownloadPopupActivity : BaseActivity<ActivityDownloadPopupBinding>() {

    @Inject
    lateinit var downloadHelper: DownloadHelper

    private val adapter = BaseListAdapter.createAdapter {
        val videos =
            intent.getParcelableArrayListExtraCompat<DownloadData>(AppExtras.EXTRA_DOWNLOAD_VIDEOS)
                ?: emptyList()
        val audios =
            intent.getParcelableArrayListExtraCompat<DownloadData>(AppExtras.EXTRA_DOWNLOAD_AUDIOS)
                ?: emptyList()
        val images =
            intent.getParcelableArrayListExtraCompat<DownloadData>(AppExtras.EXTRA_DOWNLOAD_IMAGES)
                ?: emptyList()

        if (videos.isNotEmpty()) {
            TextListModel(
                "title_video",
                getString(R.string.download_video),
                textAppearance = R.style.TextBodyMedium,
                height = 50.dp(),
                gravity = Gravity.START
            ).attachTo(this)

            videos.forEachIndexed { index, downloadData ->
                TextListModel(
                    "download_video_$index",
                    "${
                        getString(
                            R.string.download_mimetype,
                            downloadData.mimeType
                        )
                    } ${downloadData.suffix}",
                    height = 50.dp(),
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        downloadVideo(
                            downloadData.id,
                            downloadData.mimeType,
                            downloadData.downloadUrl
                        )
                    })
                ).attachTo(this)
                SizedBoxListModel("video_divider_$index", height = 1.dp()).attachTo(this)
            }
        }

        if (audios.isNotEmpty()) {
            SizedBoxListModel(
                "audio_spacing",
                height = 16.dp(),
                backgroundColor = android.R.color.transparent
            ).attachTo(this)

            TextListModel(
                "title_audio",
                getString(R.string.download_audio),
                textAppearance = R.style.TextBodyMedium,
                height = 50.dp(),
                gravity = Gravity.START
            ).attachTo(this)

            audios.forEachIndexed { index, downloadData ->
                TextListModel(
                    "download_audio_$index",
                    "${
                        getString(
                            R.string.download_mimetype,
                            downloadData.mimeType
                        )
                    } ${downloadData.suffix}",
                    height = 50.dp(),
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        downloadAudio(
                            downloadData.id,
                            downloadData.mimeType,
                            downloadData.downloadUrl
                        )
                    })
                ).attachTo(this)
                SizedBoxListModel("audio_divider_$index", height = 1.dp()).attachTo(this)
            }
        }

        if (images.isNotEmpty()) {
            val downloadData = images.first()

            SizedBoxListModel(
                "image_spacing",
                height = 16.dp(),
                backgroundColor = android.R.color.transparent
            ).attachTo(this)
            TextListModel(
                "title_image",
                getString(R.string.download_image),
                textAppearance = R.style.TextBodyMedium,
                height = 50.dp(),
                gravity = Gravity.START
            ).attachTo(this)

            SizedBoxListModel("image_title_divider", height = 1.dp()).attachTo(this)

            TextListModel(
                "download_all_image",
                "${getString(R.string.download_all_images, images.size)} (JPG)",
                textAppearance = R.style.TextBodyMedium,
                height = 50.dp(),
                gravity = Gravity.START,
                actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                    downloadImages(
                        downloadData.id,
                        images.map(DownloadData::downloadUrl)
                    )
                })
            ).attachTo(this)
        }

        SizedBoxListModel(
            "bottom_spacing",
            height = 50.dp(),
            backgroundColor = android.R.color.transparent
        ).attachTo(this)
    }

    private fun downloadVideo(id: String, mimeType: String, downloadUrl: String) {
        val outputFile =
            "sharebox_video_${id}_${System.currentTimeMillis()}.${mimeType.asFileExtension()}"
        downloadHelper.enqueueDownload(this, downloadUrl, outputFile)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun downloadAudio(id: String, mimeType: String, downloadUrl: String) {
        val outputFile =
            "sharebox_audio_${id}_${System.currentTimeMillis()}.${mimeType.asFileExtension()}"
        downloadHelper.enqueueDownload(this, downloadUrl, outputFile)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun downloadImages(id: String, urls: List<String>) {
        downloadHelper.downloadImages(this, id, urls)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                finishAndRemoveTask()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateViewBinding(): ActivityDownloadPopupBinding {
        return ActivityDownloadPopupBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.background.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        registerOnBackPressHandler {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.recyclerView.adapter = adapter
        bottomSheetBehavior = BottomSheetBehavior.from(binding.container)
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        handleDownloadPopup()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDownloadPopup()
    }

    private fun handleDownloadPopup() {
        adapter.requestBuildModelViews()
    }
}