package com.dinhlam.sharebox.utils

import com.dinhlam.sharebox.view.ShareBoxLinkPreviewView

object LinkPreviewCacheUtils {

    private val cachedData = mutableMapOf<String, ShareBoxLinkPreviewView.OpenGraphResult>()

    fun setCache(url: String, openGraphResult: ShareBoxLinkPreviewView.OpenGraphResult) {
        cachedData[url] = openGraphResult
    }

    fun getCache(url: String): ShareBoxLinkPreviewView.OpenGraphResult? {
        return cachedData[url]
    }
}