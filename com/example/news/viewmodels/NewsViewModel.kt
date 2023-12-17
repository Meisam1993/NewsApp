package com.example.news.viewmodels

import androidx.lifecycle.ViewModel
import com.example.news.repository.NewsRepository

class NewsViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {

}