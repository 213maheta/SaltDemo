package com.twoonethree.saltdemo.data.mapper

import com.twoonethree.saltdemo.data.local.entity.ArticleEntity
import com.twoonethree.saltdemo.data.remote.dto.ArticleDto
import com.twoonethree.saltdemo.domain.model.Article

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
