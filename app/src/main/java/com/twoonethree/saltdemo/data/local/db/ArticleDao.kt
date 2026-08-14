package com.twoonethree.saltdemo.data.local.db

import androidx.room.*
import com.twoonethree.saltdemo.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE url = :url LIMIT 1")
    suspend fun getArticleByUrl(url: String): ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isBookmarked = :bookmarked WHERE url = :url")
    suspend fun setBookmarked(url: String, bookmarked: Boolean)

    @Query("DELETE FROM articles WHERE category = :category AND isBookmarked = 0")
    suspend fun clearCategoryCache(category: String)

    @Query("SELECT * FROM articles WHERE url = :url LIMIT 1")
    fun observeArticleByUrl(url: String): Flow<ArticleEntity?>

    @Query("""
    SELECT * FROM articles 
    WHERE title LIKE '%' || :query || '%' 
       OR description LIKE '%' || :query || '%'
    ORDER BY publishedAt DESC
""")
    fun searchArticles(query: String): Flow<List<ArticleEntity>>
}
