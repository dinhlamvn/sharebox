package com.dinhlam.sharebox.ui.home.bookmark.form

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.model.BookmarkCollectionDetail
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.extensions.md5
import com.dinhlam.sharebox.extensions.takeIfNotNullOrBlank
import com.dinhlam.sharebox.logger.Logger
import com.dinhlam.sharebox.utils.BookmarkUtils
import com.dinhlam.sharebox.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookmarkCollectionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookmarkCollectionRepository: BookmarkCollectionRepository
) : BaseViewModel<BookmarkCollectionFormState>(BookmarkCollectionFormState().run {
    val collectionDetail: BookmarkCollectionDetail? =
        savedStateHandle[AppExtras.EXTRA_BOOKMARK_COLLECTION]
    copy(bookmarkCollectionDetail = collectionDetail,
        thumbnail = collectionDetail?.let { collection -> Uri.parse(collection.thumbnail) })
}) {

    fun setThumbnail(uri: Uri) {
        setState { copy(thumbnail = uri, errorThumbnail = false) }
    }

    fun performActionDone(context: Context, name: String, desc: String) {
        execute(Dispatchers.IO, onError = {
            Logger.error("$it")
        }) { state ->
            setState { copy(errorName = null, errorDesc = null) }

            if (name.isEmpty()) {
                return@execute setState { copy(errorName = R.string.bookmark_collection_error_require_name) }
            }

            if (desc.isEmpty()) {
                return@execute setState { copy(errorDesc = R.string.bookmark_collection_error_require_desc) }
            }

            val thumbnail =
                state.thumbnail ?: return@execute setState { copy(errorThumbnail = true) }
            context.contentResolver.openInputStream(thumbnail)?.use { inputStream ->
                val thumbnailFileDir =
                    context.getExternalFilesDir("bookmark_collection_thumbnails")!!

                if (!thumbnailFileDir.exists() && !thumbnailFileDir.mkdir()) {
                    return@use null
                }
                val extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(context.contentResolver.getType(thumbnail))
                    ?: return@use null

                val imageFile =
                    File(thumbnailFileDir, "thumbnail_${System.currentTimeMillis()}.$extension")

                if (!imageFile.createNewFile()) {
                    return@use null
                }

                imageFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                val newUri = FileUtils.getUriFromFile(context, imageFile)

                val result = state.bookmarkCollectionDetail?.id?.let { collectionId ->
                    bookmarkCollectionRepository.updateCollection(collectionId) {
                        copy(name = name, description = desc, thumbnail = newUri.toString()).run {
                            state.passcode.takeIfNotNullOrBlank()?.let { newPasscode ->
                                copy(passcode = newPasscode.md5())
                            } ?: this
                        }
                    }
                } ?: bookmarkCollectionRepository.createCollection(
                    BookmarkUtils.createBookmarkCollectionId(),
                    name,
                    desc,
                    newUri.toString(),
                    state.passcode
                )

                if (result) {
                    setState { copy(success = true) }
                } else {
                    postShowToast(R.string.bookmark_collection_create_error)
                }
            } ?: return@execute postShowToast(R.string.bookmark_collection_create_error)
        }
    }

    fun clearErrorName(text: String?) = getState { state ->
        if (state.errorName != null && !text.isNullOrBlank()) {
            setState { copy(errorName = null) }
        }
    }

    fun clearErrorDesc(text: String?) = getState { state ->
        if (state.errorDesc != null && !text.isNullOrBlank()) {
            setState { copy(errorDesc = null) }
        }
    }

    fun setPasscode(passcode: String) {
        setState { copy(passcode = passcode, isPasscodeVisible = false) }
    }

    fun togglePasscodeVisibility() {
        setState { copy(isPasscodeVisible = !isPasscodeVisible) }
    }

    fun clearPasscode() {
        setState { copy(passcode = "") }
    }

}