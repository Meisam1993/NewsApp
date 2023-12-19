package com.example.news.repository

import com.example.news.api.RetrofitInstance
import com.example.news.db.ArticleDao

class NewsRepository(
    private val articleDao: ArticleDao
) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.apiService.getBreakingNews(countryCode, pageNumber)

    suspend fun searchForNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.apiService.searchForNews(searchQuery, pageNumber)
}