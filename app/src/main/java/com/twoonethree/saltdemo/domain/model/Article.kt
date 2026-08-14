package com.twoonethree.saltdemo.domain.model

data class Article(
    val url: String,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val sourceName: String,
    val publishedAt: String,
    val content: String?,
    val category: String,
    val isBookmarked: Boolean = false
)
