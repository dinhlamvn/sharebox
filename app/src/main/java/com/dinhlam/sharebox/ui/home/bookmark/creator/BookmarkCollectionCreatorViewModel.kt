package com.dinhlam.sharebox.ui.home.bookmark.creator

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.dinhlam.sharebox.BuildConfig
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.data.repository.BookmarkCollectionRepository
import com.dinhlam.sharebox.imageloader.ImageLoader
import com.dinhlam.sharebox.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookmarkCollectionCreatorViewModel @Inject constructor(
    private val bookmarkCollectionRepository: BookmarkCollectionRepository
) : BaseViewModel<BookmarkCollectionCreatorState>(BookmarkCollectionCreatorState()) {

    fun setThumbnail(uri: Uri) {
        setState { copy(thumbnail = uri, errorThumbnail = false) }
    }

    fun createBookmarkCollection(context: Context, name: String, desc: String) {
        execute(Dispatchers.IO, onError = {
            Logger.debug("error $it")
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
            val bitmap = ImageLoader.instance.get(context, thumbnail) ?: return@execute
            val imagePath = context.getExternalFilesDir("bookmark_collection_thumbnails")!!
            if (!imagePath.exists()) {
                imagePath.mkdir()
            }
            val imageFile = File(imagePath, "thumbnail_${System.currentTimeMillis()}.jpg")
            imageFile.createNewFile()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, imageFile.outputStream())
            val newUri = FileProvider.getUriForFile(
                context, "${BuildConfig.APPLICATION_ID}.file_provider", imageFile
            )

            val result =
                bookmarkCollectionRepository.createCollection(
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
        setState { copy(passcode = passcode) }
    }

    fun clearPasscode() {
        setState { copy(passcode = "") }
    }

}