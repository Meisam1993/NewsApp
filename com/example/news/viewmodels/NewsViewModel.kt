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
    var breakingNewsPageNumber = 1
    private var breakingNewsResponse: NewsResponse? = null

    val searchNewsLiveData: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPageNumber = 1
    private var searchNewsResponse: NewsResponse? = null

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
            response.body()?.let { resultResponse ->
                breakingNewsPageNumber++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    if (newArticles != null) {
                        oldArticles?.addAll(newArticles)
                    }
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchForNewsResponse(
        response: Response<NewsResponse>
    )
            : Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPageNumber++
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    if (newArticles !== null) {
                        oldArticles?.addAll(newArticles)
                    }
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
}