package com.dinhlam.sharebox.ui.discover.unsplash

import com.dinhlam.sharebox.base.BaseViewModel.AsyncLoad
import com.dinhlam.sharebox.model.UnsplashPhoto
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnsplashDiscoverStateTest {
    private val photos = List(20) { index ->
        UnsplashPhoto(
            id = "$index",
            imageUrl = "https://example.com/$index.jpg",
            downloadLocation = "https://example.com/$index/download",
            width = 100,
            height = 100,
            photographerName = "Photographer",
            photographerUrl = "https://example.com/photographer",
            photoUrl = "https://example.com/photos/$index",
        )
    }
    private val loaded = UnsplashDiscoverState(
        query = "nature",
        photos = photos,
        page = 1,
        totalPages = 3,
        asyncSearch = AsyncLoad.Success(photos),
    )

    @Test
    fun loadsOnlyNearTheEndOfVisibleResults() {
        assertFalse(loaded.shouldLoadMore(-1))
        assertFalse(loaded.shouldLoadMore(14))
        assertTrue(loaded.shouldLoadMore(15))
        assertTrue(loaded.shouldLoadMore(19))
    }

    @Test
    fun waitsForCurrentRequestThenCanContinueWithoutAnotherScroll() {
        assertFalse(loaded.copy(isLoadingMore = true).shouldLoadMore(19))
        assertTrue(loaded.copy(page = 2).shouldLoadMore(19))
    }

    @Test
    fun doesNotLoadBeforeSuccessfulSearchOrAfterLastPage() {
        assertFalse(UnsplashDiscoverState().canLoadMore)
        assertFalse(loaded.copy(query = " ").canLoadMore)
        assertFalse(loaded.copy(photos = emptyList()).canLoadMore)
        assertFalse(loaded.copy(page = 0).canLoadMore)
        assertFalse(loaded.copy(asyncSearch = AsyncLoad.Loading).canLoadMore)
        assertFalse(loaded.copy(asyncSearch = AsyncLoad.Failed(Exception())).canLoadMore)
        assertFalse(loaded.copy(page = 3).canLoadMore)
    }

    @Test
    fun failureAllowsManualRetryButStopsAutomaticRetries() {
        val failed = loaded.copy(loadMoreError = "Network error")
        assertFalse(failed.shouldLoadMore(19))
        assertTrue(failed.canLoadMore)
        assertTrue(failed.copy(loadMoreError = null).shouldLoadMore(19))
    }
}
