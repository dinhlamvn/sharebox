package com.dinhlam.sharebox.model

import com.google.gson.annotations.SerializedName

data class TiktokCategory(
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("categoryName")
    val categoryName: String
) {
    companion object {
        val categories = listOf(
            TiktokCategory(119, "Singing & Dancing"),
            TiktokCategory(104, "Comedy"),
            TiktokCategory(112, "Sports"),
            TiktokCategory(100, "Anime & Comic"),
            TiktokCategory(107, "Relationship"),
            TiktokCategory(101, "Shows"),
            TiktokCategory(110, "Lipsync"),
            TiktokCategory(105, "Daily Life"),
            TiktokCategory(102, "Beauty Care"),
            TiktokCategory(103, "Games"),
            TiktokCategory(114, "Society"),
            TiktokCategory(109, "Outfit"),
            TiktokCategory(115, "Cars"),
            TiktokCategory(111, "Food"),
            TiktokCategory(113, "Animals"),
            TiktokCategory(106, "Family"),
            TiktokCategory(108, "Drama"),
            TiktokCategory(117, "Fitness & Health"),
            TiktokCategory(116, "Education"),
            TiktokCategory(118, "Technology"),
        )
    }
}
