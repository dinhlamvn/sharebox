package com.dinhlam.sharebox.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.common.AppConsts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.repository.BookmarkRepository
import com.dinhlam.sharebox.data.repository.CommentRepository
import com.dinhlam.sharebox.data.repository.LikeRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import com.dinhlam.sharebox.dialog.bookmarkcollectionpicker.BookmarkCollectionPickerDialogFragment
import com.dinhlam.sharebox.dialog.box.BoxSelectionDialogFragment
import com.dinhlam.sharebox.dialog.sharelink.ShareLinkInputDialogFragment
import com.dinhlam.sharebox.dialog.sharetextquote.ShareTextQuoteInputDialogFragment
import com.dinhlam.sharebox.dialog.text.TextViewerDialogFragment
import com.dinhlam.sharebox.dialog.viewimages.ViewImagesDialogFragment
import com.dinhlam.sharebox.extensions.cast
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.queryIntentActivitiesCompat
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.ShareDetail
import com.dinhlam.sharebox.model.VideoSource
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.comment.CommentFragment
import com.dinhlam.sharebox.ui.sharereceive.ShareReceiveActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val router: Router,
    private val shareRepository: ShareRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val userHelper: UserHelper,
) {

    fun shareToOther(share: ShareDetail) {
        val intent = Intent(Intent.ACTION_SEND)
        when (val shareData = share.shareData) {
            is ShareData.ShareUrl -> {
                intent.putExtra(Intent.EXTRA_TEXT, shareData.castNonNull<ShareData.ShareUrl>().url)
                intent.type = "text/*"
            }

            is ShareData.ShareText -> {
                intent.putExtra(
                    Intent.EXTRA_TEXT, shareData.castNonNull<ShareData.ShareText>().text
                )
                intent.type = "text/*"
            }

            is ShareData.ShareImage -> {
                val shareImage = shareData.castNonNull<ShareData.ShareImage>()
                intent.putExtra(
                    Intent.EXTRA_STREAM, shareImage.uri
                )
                intent.setDataAndType(shareImage.uri, "image/*")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            is ShareData.ShareImages -> {
                val data = shareData.castNonNull<ShareData.ShareImages>()
                intent.action = Intent.ACTION_SEND_MULTIPLE
                intent.putParcelableArrayListExtra(
                    Intent.EXTRA_STREAM, arrayListOf(*data.uris.toTypedArray())
                )
                intent.setDataAndType(data.uris[0], "image/*")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val components = arrayOf(ComponentName(context, ShareReceiveActivity::class.java))
            val chooser = Intent.createChooser(intent, "Share To")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, components)
            context.startActivity(chooser)
        } else {
            val resolveInfoList = context.packageManager.queryIntentActivitiesCompat(intent, 0)
            if (resolveInfoList.isNotEmpty()) {
                val targetIntents = mutableListOf<Intent>()
                resolveInfoList.forEach { resolveInfo ->
                    val newIntent = Intent(intent)
                    if (!resolveInfo.activityInfo.packageName.equals(
                            BuildConfig.APPLICATION_ID, true
                        )
                    ) {
                        newIntent.setPackage(resolveInfo.activityInfo.packageName)
                        targetIntents.add(intent)
                    }
                }
                if (targetIntents.isEmpty()) {
                    return
                }

                val chooserIntent = Intent.createChooser(targetIntents.removeAt(0), "Share To")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            }
        }
    }

    fun openUrl(context: Context, url: String, isEnableCustomTab: Boolean = true) {
        if (isEnableCustomTab) {
            router.moveToChromeCustomTab(context, url)
        } else {
            router.moveToBrowser(url)
        }
    }

    fun openTextViewerDialog(activity: FragmentActivity, text: String) {
        TextViewerDialogFragment().apply {
            arguments = Bundle().apply {
                putString(Intent.EXTRA_TEXT, text)
            }
        }.show(activity.supportFragmentManager, "TextViewerDialogFragment")
    }

    fun viewShareImage(activity: FragmentActivity, shareId: String, uri: Uri) {
        ViewImagesDialogFragment().apply {
            arguments = Bundle().apply {
                putString(AppExtras.EXTRA_SHARE_ID, shareId)
                putParcelableArrayList(
                    AppExtras.EXTRA_IMAGE_URIS, arrayListOf(uri)
                )
            }
        }.show(activity.supportFragmentManager, "ViewImagesDialogFragment")
    }

    fun viewShareImages(activity: FragmentActivity, shareId: String, uris: List<Uri>) {
        ViewImagesDialogFragment().apply {
            arguments = Bundle().apply {
                putString(AppExtras.EXTRA_SHARE_ID, shareId)
                putParcelableArrayList(
                    AppExtras.EXTRA_IMAGE_URIS, arrayListOf(*uris.toTypedArray())
                )
            }
        }.show(activity.supportFragmentManager, "ViewImagesDialogFragment")
    }

    fun showBookmarkCollectionPickerDialog(
        fragmentManager: FragmentManager,
        shareId: String,
        collectionId: String?,
    ) {
        BookmarkCollectionPickerDialogFragment().apply {
            arguments = Bundle().apply {
                putString(AppExtras.EXTRA_SHARE_ID, shareId)
                putString(AppExtras.EXTRA_BOOKMARK_COLLECTION_ID, collectionId)
            }
        }.show(fragmentManager, "BookmarkCollectionPickerDialogFragment")
    }

    fun showCommentDialog(fragmentManager: FragmentManager, shareId: String) {
        CommentFragment().apply {
            arguments = Bundle().apply {
                putString(AppExtras.EXTRA_SHARE_ID, shareId)
            }
        }.show(fragmentManager, "CommentFragment")
    }

    fun showBoxSelectionDialog(fragmentManager: FragmentManager) {
        BoxSelectionDialogFragment().show(fragmentManager, "BoxSelectionDialogFragment")
    }

    fun shareTextQuote(fragmentManager: FragmentManager) {
        ShareTextQuoteInputDialogFragment().show(
            fragmentManager, "ShareTextQuoteInputDialogFragment"
        )
    }

    fun shareLink(fragmentManager: FragmentManager) {
        ShareLinkInputDialogFragment().show(fragmentManager, "ShareLinkInputDialogFragment")
    }

    suspend fun calcTrendingScore(shareId: String): Int {
        val share = shareRepository.findOneRaw(shareId) ?: return 0

        var trendingScore = 0

        val commentCountByCurrentUser =
            commentRepository.count(shareId, userId = userHelper.getCurrentUserId())
        trendingScore += commentCountByCurrentUser.times(2)

        if (likeRepository.liked(shareId, userHelper.getCurrentUserId())) {
            trendingScore += 10
        }

        if (bookmarkRepository.bookmarked(shareId)) {
            trendingScore += 15
        }

        val commentCount = commentRepository.count(shareId)
        trendingScore += (commentCount / 5)

        val likeCount = likeRepository.count(shareId)
        trendingScore += likeCount

        val elapsed = nowUTCTimeInMillis() - share.shareDate
        val hours = elapsed.div(3600 * 1000).toInt()

        return trendingScore.minus(hours)
    }

    fun viewInSource(context: Context, videoSource: VideoSource, shareData: ShareData) {
        when (videoSource) {
            is VideoSource.Tiktok -> viewInTiktok(context, shareData)
            is VideoSource.Youtube -> viewInYoutube(context, shareData)
            is VideoSource.Facebook -> viewInFacebook(context, shareData)
        }
    }

    private fun viewInTiktok(context: Context, shareData: ShareData) {
        val shareUrl = shareData.cast<ShareData.ShareUrl>() ?: return
        val viewIntent = router.viewIntent(shareUrl.url)

        val resolveInfoList = context.packageManager?.queryIntentActivitiesCompat(
            viewIntent, PackageManager.GET_META_DATA
        ) ?: return

        resolveInfoList.run stop@{
            forEach { resolveInfo ->
                if (resolveInfo.activityInfo.packageName.equals(AppConsts.TIKTOK_M_PACKAGE_ID)) {
                    viewIntent.setPackage(AppConsts.TIKTOK_M_PACKAGE_ID)
                    return@stop
                }

                if (resolveInfo.activityInfo.packageName.equals(AppConsts.TIKTOK_O_PACKAGE_ID)) {
                    viewIntent.setPackage(AppConsts.TIKTOK_O_PACKAGE_ID)
                    return@stop
                }
            }
        }

        if (viewIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(viewIntent)
        } else {
            context.startActivity(router.playStoreIntent(AppConsts.TIKTOK_M_PACKAGE_ID))
        }
    }

    private fun viewInYoutube(context: Context, shareData: ShareData) {
        val shareUrl = shareData.cast<ShareData.ShareUrl>() ?: return
        val viewIntent = router.viewIntent(shareUrl.url)
        viewIntent.runCatching {
            context.startActivity(this)
        }.onFailure { error ->
            Logger.error(error)
            context.startActivity(router.playStoreIntent(AppConsts.YOUTUBE_M_PACKAGE_ID))
        }
    }

    private fun viewInFacebook(context: Context, shareData: ShareData) {
        val shareUrl = shareData.cast<ShareData.ShareUrl>() ?: return
        val viewIntent = router.viewIntent(shareUrl.url)
        viewIntent.setPackage(AppConsts.FACEBOOK_M_PACKAGE_ID)

        if (viewIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(viewIntent)
        } else {
            context.startActivity(router.playStoreIntent(AppConsts.FACEBOOK_M_PACKAGE_ID))
        }
    }
}
