package com.dinhlam.sharebox.data.repository

import android.net.Uri
import com.dinhlam.sharebox.data.network.PinterestServices
import com.dinhlam.sharebox.model.PinterestPin
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinterestRepository @Inject constructor(
    private val services: PinterestServices,
) {
    suspend fun search(query: String, page: Int = 1): List<PinterestPin> {
        val searchUrl = buildSearchUrl(query, page)
        val html = services.search(searchUrl, MOBILE_USER_AGENT).string()
        return parseSearchHtml(html)
    }

    fun buildSearchUrl(query: String, page: Int = 1): String {
        return Uri.Builder()
            .scheme("https")
            .authority("www.pinterest.com")
            .appendPath("search")
            .appendPath("pins")
            .appendQueryParameter("q", query.trim())
            .apply {
                if (page > 1) {
                    appendQueryParameter("page", page.toString())
                }
            }
            .build()
            .toString()
    }

    internal fun parseSearchHtml(html: String): List<PinterestPin> {
        val json = Jsoup.parse(html)
            .getElementById(INITIAL_PROPS_SCRIPT_ID)
            ?.data()
            ?.takeIf(String::isNotBlank)
            ?: return emptyList()
        val pins = JsonParser.parseString(json)
            .asJsonObject
            .objectAt("initialReduxState")
            ?.objectAt("pins")
            ?: return emptyList()

        return pins.entrySet().mapNotNull { (_, element) ->
            val pin = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val id = pin.stringAt("id") ?: return@mapNotNull null
            val imageUrl = pin.objectAt("images")
                ?.let(::bestImageUrl)
                ?: return@mapNotNull null
            val description = pin.stringAt("description")
            PinterestPin(
                id = id,
                title = pin.stringAt("grid_title")
                    ?: description
                    ?: pin.stringAt("seo_alt_text")
                    ?: id,
                description = description,
                imageUrl = imageUrl,
            )
        }.distinctBy(PinterestPin::id)
    }

    private fun bestImageUrl(images: JsonObject): String? {
        return IMAGE_VARIANTS.firstNotNullOfOrNull { variant ->
            images.objectAt(variant)?.stringAt("url")
        }
    }

    private fun JsonObject.objectAt(name: String): JsonObject? =
        get(name)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun JsonObject.stringAt(name: String): String? =
        get(name)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
            ?.asString
            ?.takeIf(String::isNotBlank)

    private companion object {
        const val INITIAL_PROPS_SCRIPT_ID = "__PWS_INITIAL_PROPS__"
        const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124 Mobile Safari/537.36"
        val IMAGE_VARIANTS = listOf("736x", "474x", "236x", "170x")
    }
}
