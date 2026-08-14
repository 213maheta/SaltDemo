package com.twoonethree.saltdemo.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.twoonethree.saltdemo.data.local.entity.ArticleEntity

@Database(entities = [ArticleEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
