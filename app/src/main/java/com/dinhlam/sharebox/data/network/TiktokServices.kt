package com.dinhlam.sharebox.data.network

import com.dinhlam.sharebox.data.network.response.TiktokExploreResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.QueryMap

interface TiktokServices {

    @GET("api/explore/item_list")
    @Headers(
        "Accept: */*",
        "Cookie: _ttp=2OiWTjSGcODxC0EZwpj9mI9yrpD; tt_chain_token=JdRS0V2P/e+D/n/zezBJ5A==; store-country-code-src=uid; tiktok_webapp_theme_auto_dark_ab=1; delay_guest_mode_vid=8; odin_tt=5a902b5f74ff61d7463f934d2e23a7f3c8c05941072ef3062f7920f7b4aca578dcb10ff988dc2eb4e28dec777b19a596ce1252274d3db9fa0daf25721d705e947c2df5de3ab79412724b92e24acd154f; _ga=GA1.1.1529794018.1729089365; FPID=FPID2.2.f7iGebdYKTMDIZUYwH3OLAIktKJoT7ZrcNCihhLs7Bc%3D.1729089365; FPAU=1.2.589385907.1729089366; FPLC=0trgwafnoKpSeScLP%2BTVc2W0i8jsuVx%2BgGQtdas12ohm8gzViM6ksDHR%2BdELNQkQzeprocjq02VS6xlLSd8zK7Mu0GKD5o9zCo7qXBz7qNLw63G09E4UFlHuM3TC4g%3D%3D; _ga_QQM0HPKD40=GS1.1.1729089365.1.1.1729089382.0.0.1822401593; tt_csrf_token=p4qq1D8Q-PXv8-2z3exbplQtoAzYblDQyxYk; ak_bmsc=72CA5EB661B36AE0DF255AD54F180B07~000000000000000000000000000000~YAAQfL4vF1gPrpKSAQAA1rrplRntJYgPD6Of2HqI4U6soUippA0sa/YLdHc9GBkJ9RGET5UNLYrVzZWkmp87phr0lzEoLfFi+KUItfQ8j/aQ0z8K0asEmyiT3rwpt2PieKxTyhoy/YgZF2XDiCRM6XpbVfxwrskZKDKKRiEMVjtCQD0jcDR4C7cpxPFczxaMtRink6jqZMwPIPRfQCwZZmKNERfqxreMRGLZYxjFiY4ZNLbN1DgVh9r7aa5UaRMGqOI74tShxzK69tb7G4Im2MLA18lNafHYHHQkZWGsTdrnQwkw7T0Bf92SjYwvDg/Ay1Ya6RTaYt1C1lVQl1bk7PQ+OJwOMoy8MoyQ9oeRSxlONLmA6diFU62BeMUBu2ccV8zAoRhSbV/bHRuT; tiktok_webapp_theme_source=auto; tiktok_webapp_theme=dark; perf_feed_cache={%22expireTimestamp%22:1729263600000%2C%22itemIds%22:[%227421187813787438343%22%2C%227424078289045654792%22%2C%227422501142602501383%22]}; msToken=WaYMyRtvjwYA9jna7clQXiuvMM3tEcARRSSfM8PUIv8cDmE8l33aeQSdR0gKHgflrUvF9I91El9DFT9Z1Kq9Uq8J0Z8NgAQUw1w1qdzjEohewFD2ihs02oB6HqmxyhraeuV2YUWuI74pTbx-XQZXCoc=; msToken=GqEGitXPcW4esrk7yVfGk4kJBdIYtuHeBtV_hImNyH4qmoXtw4dBeMFJ957KsaKS0vTWyNuxRnOM8d3kKlMTv_USrL1FTHj_KxqQObe6nXXo34ReaFu-elOFFTHo; bm_sv=3D309AB3DE97BDC2F240C3058603A652~YAAQX74vFx4KW5WSAQAAvT/qlRmVEY7ZkacyykZM2R0u6ris8wtOBxu74Ej5Y/F/DFGleijTDW2E09/LtpFgUr2aaK/WlP4yESgOO8Y9ZfUK5ag1H1i4+xw7oSegXUBe1bg2i8oId8FUdJuetCW2Wc37oumFUOxu5MAd5IwS2ycYV3n/SxwcONvuuTPpj45mwQuT3aFu2xIWoXQAw8ebgFBz8KyMtKdlUW02iJm5FkZZ1VSVfW5FIv0yEswyB9BQ~1; ttwid=1%7CND3flbKHv4NC2Q_7PW_7n2yG6ls4cqTd1eczd--ssJA%7C1729092010%7C5c992841b52e07a4f7d48802199053c277475f7132ef24ef40a746eeb12ba908; msToken=OYn4DJLU-zjOzBhe87lDNYViAu5Tk01Tm-mK02TVrA0jxRB2RTi0pHX3Yfv7OswF6UDWwkDraAr6UZGuMzS1DJoQaens5YhRFSRlZRyqJREiEwZt1rclM8kxnJUxS3ymd3KITp99SH1xwm21qQr7g58="
    )
    suspend fun explore(
        @Header("User-Agent") userAgent: String,
        @QueryMap queryMap: Map<String, String>
    ): TiktokExploreResponse
}