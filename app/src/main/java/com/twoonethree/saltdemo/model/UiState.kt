package com.twoonethree.saltdemo.model

import com.twoonethree.saltdemo.room.ArticleEntity

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


fun ArticleEntity.toDomain(): Article = Article(
    url = url,
    title = title,
    description = description,
    urlToImage = urlToImage,
    sourceName = sourceName,
    publishedAt = publishedAt,
    content = content,
    category = category,
    isBookmarked = isBookmarked
)


fun ArticleDto.toEntity(category: String): ArticleEntity = ArticleEntity(
    url = url,
    title = title,
    description = description,
    urlToImage = urlToImage,
    sourceName = source.name,
    publishedAt = publishedAt,
    content = content,
    category = category
)