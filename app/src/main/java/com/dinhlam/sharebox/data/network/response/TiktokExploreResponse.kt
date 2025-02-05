package com.dinhlam.sharebox.data.network.response

import com.google.gson.annotations.SerializedName

data class TiktokExploreResponse(
    @SerializedName("itemList")
    val itemList: List<Item>
) {
    data class Item(
        @SerializedName("id")
        val id: String,
        @SerializedName("author")
        val author: Author,
        @SerializedName("video")
        val video: Video,
        @SerializedName("stats")
        val stats: Stats,
        @SerializedName("desc")
        val desc: String?
    )

    data class Author(
        @SerializedName("uniqueId")
        val uniqueId: String
    )

    data class Video(
        @SerializedName("id")
        val id: String
    )

    data class Stats(
        @SerializedName("playCount")
        val playCount: Long,
        @SerializedName("commentCount")
        val commentCount: Long,
        @SerializedName("diggCount")
        val diggCount: Long,
        @SerializedName("shareCount")
        val shareCount: Long
    )

}
