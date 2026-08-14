package com.twoonethree.saltdemo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val sourceName: String,
    val publishedAt: String,
    val content: String?,
    val category: String,
    val isBookmarked: Boolean = false,
    val fetchedAt: Long = System.currentTimeMillis()
)
