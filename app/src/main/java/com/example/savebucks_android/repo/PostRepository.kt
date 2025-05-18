package com.example.savebucks_android.repo

import com.example.savebucks_android.datamodel.PostUiModel
import com.example.savebucks_android.datamodel.WordPressPost
import com.example.savebucks_android.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class PostRepository {
    private val apiService = RetrofitClient.wordPressService

    suspend fun getPosts(page: Int = 1, perPage: Int = 20): Result<List<PostUiModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val posts = apiService.getPosts(perPage, page)
                Result.success(posts.map { it.toUiModel() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun WordPressPost.toUiModel(): PostUiModel {
        // Extract the image URL from the HTML content if featured media is not available
        val doc = Jsoup.parse(content.rendered)
        val imageUrl = links.featuredMedia?.firstOrNull()?.href ?:
        doc.select("img").firstOrNull()?.attr("src") ?:
        ""

        // Clean the excerpt from HTML tags
        val cleanExcerpt = Jsoup.parse(excerpt.rendered).text()

        return PostUiModel(
            id = id,
            title = title.rendered,
            excerpt = cleanExcerpt,
            date = date,
            imageUrl = imageUrl,
            slug = slug,
            content = content.rendered
        )
    }
}