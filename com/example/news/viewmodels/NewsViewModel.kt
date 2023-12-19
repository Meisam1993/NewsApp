package com.example.news.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.news.model.Articles
import com.example.news.model.NewsResponse
import com.example.news.repository.NewsRepository
import com.example.news.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {
    val breakingNewsLiveData: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    private val breakingNewsPageNumber = 1

    val searchNewsLiveData: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    private val searchNewsPageNumber = 1

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        breakingNewsLiveData.postValue(Resource.Loading())
        val response = newsRepository.getBreakingNews(countryCode, breakingNewsPageNumber)
        breakingNewsLiveData.postValue(handleBreakingNewsResponse(response))
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsLiveData.postValue(Resource.Loading())
        val response = newsRepository.searchForNews(searchQuery, searchNewsPageNumber)
        searchNewsLiveData.postValue(handleSearchForNewsResponse(response))
    }

    fun saveArticle(articles: Articles) = viewModelScope.launch {
        newsRepository.insertOrUpdateArticle(articles)
    }

    fun getAllSavedArticles() = newsRepository.getAllSavedArticles()


    fun deleteArticle(articles: Articles) = viewModelScope.launch {
        newsRepository.deleteArticle(articles)
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchForNewsResponse(
        response: Response<NewsResponse>
    )
            : Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }
}