package com.example.news.model

data class NewsResponse(
	val totalResults: Int? = null,
	val articles: MutableList<Articles?>? = null,
	val status: String? = null
)

