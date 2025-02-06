package com.dinhlam.sharebox.data.network.response

import com.google.gson.annotations.SerializedName

data class AppDLResponse(
    @SerializedName("itemId")
    val itemId: String,
    @SerializedName("original")
    val original: String,
    @SerializedName("aweme_link")
    val awemeLink: String,
    @SerializedName("music_link")
    val musicLink: String,
    @SerializedName("watermark_link")
    val watermarkLink: String,
    @SerializedName("no_watermark_link")
    val noWatermarkLink: String,
    @SerializedName("no_watermark_link_hd")
    val noWatermarkLinkHd: String,
    @SerializedName("cover_link")
    val coverLink: String,
    @SerializedName("author_cover_link")
    val authorCoverLink: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("create_time")
    val createTime: String,
    @SerializedName("duration")
    val duration: String,
    @SerializedName("author_unique_id")
    val authorUniqueId: String,
    @SerializedName("author_nickname")
    val authorNickname: String,
    @SerializedName("author_id")
    val authorId: String,
    @SerializedName("comment_count")
    val commentCount: String,
    @SerializedName("play_count")
    val playCount: String,
    @SerializedName("share_count")
    val shareCount: String,
    @SerializedName("like_count")
    val likeCount: String,
    @SerializedName("origin_cover")
    val originCover: String,
    @SerializedName("signed")
    val signed: Long,
    @SerializedName("type")
    val type: Long,
    @SerializedName("slides")
    val slides: Slides?
) {

    data class Slides(
        val slideDataList: List<SlideData>,
        val music: String?
    )

    data class SlideData(
        @SerializedName("url")
        val url: String,
        @SerializedName("width")
        val width: Int,
        @SerializedName("height")
        val height: Int
    )
}
