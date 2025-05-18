package com.example.savebucks_android.datamodel

import com.google.gson.annotations.SerializedName

data class WordPressPost(
    val id: Int,
    val date: String,
    val title: Title,
    val excerpt: Excerpt,
    @SerializedName("_links")
    val links: Links,
    @SerializedName("featured_media")
    val featuredMedia: Int = 0,
    val slug: String,
    val content: Content
)

data class Title(
    val rendered: String
)

data class Excerpt(
    val rendered: String
)

data class Content(
    val rendered: String
)

data class Links(
    @SerializedName("wp:featuredmedia")
    val featuredMedia: List<FeaturedMedia>? = null
)

data class FeaturedMedia(
    val href: String
)

// Simplified model for UI display
data class PostUiModel(
    val id: Int,
    val title: String,
    val excerpt: String,
    val date: String,
    val imageUrl: String = "",
    val slug: String,
    val content: String
)