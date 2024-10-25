package com.dinhlam.sharebox.model

data class ZingNewsCategory(
    val id: String,
    val name: String,
    val url: String
) {

    companion object {
        val categories = arrayOf(
            ZingNewsCategory("xuat-ban", "Xuất bản", "https://znews.vn/xuat-ban.html"),
            ZingNewsCategory("cong-nghe", "Công nghệ", "https://znews.vn/cong-nghe.html"),
            ZingNewsCategory("tac-gia", "Tác giả", "https://znews.vn/tac-gia.html"),
            ZingNewsCategory(
                "kd-tc",
                "Kinh doanh & tài chính",
                "https://znews.vn/kinh-doanh-tai-chinh.html"
            ),
            ZingNewsCategory("doi-song", "Đời sống", "https://lifestyle.znews.vn/doi-song.html"),
            ZingNewsCategory("suc-khoe", "Sức khỏe", "https://lifestyle.znews.vn/suc-khoe.html"),
            ZingNewsCategory("the-thao", "Thể thao", "https://znews.vn/the-thao.html"),
            ZingNewsCategory("du-lich", "Du lịch", "https://lifestyle.znews.vn/du-lich.html"),
            ZingNewsCategory("phap-luat", "Pháp luật", "https://lifestyle.znews.vn/phap-luat.html"),
            ZingNewsCategory("giao-duc", "Giáo dục", "https://lifestyle.znews.vn/giao-duc.html"),
            ZingNewsCategory("thoi-su", "Thời sự", "https://znews.vn/thoi-su.html"),
            ZingNewsCategory("the-gioi", "Thế giới", "https://znews.vn/the-gioi.html"),
            ZingNewsCategory("giai-tri", "Giải trí", "https://znews.vn/giai-tri.html"),
            ZingNewsCategory(
                "lifestyle",
                "Phong cách sống",
                "https://lifestyle.znews.vn/lifestyle.html"
            ),
        )
    }
}