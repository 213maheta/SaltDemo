package com.twoonethree.saltdemo.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,         // NewsAPI articles don't have IDs, url is unique per article
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val sourceName: String,
    val publishedAt: String,
    val content: String?,
    val category: String,                // stores NewsCategory.apiValue
    val isBookmarked: Boolean = false,
    val fetchedAt: Long = System.currentTimeMillis()  // for cache freshness checks
)