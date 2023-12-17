package com.example.news.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.news.model.Articles

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateOrInsert(articles: Articles): Long

    @Query("SELECT * FROM articles")
    fun getAllArticles(): LiveData<List<Articles>>

    @Delete
    suspend fun deleteFromArticles(articles: Articles)
}