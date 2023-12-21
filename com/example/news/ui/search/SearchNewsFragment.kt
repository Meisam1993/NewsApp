package com.example.news.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.adapters.NewsAdapter
import com.example.news.databinding.FragmentSearchNewsBinding
import com.example.news.db.ArticleDatabase
import com.example.news.model.Articles
import com.example.news.repository.NewsRepository
import com.example.news.utils.Constants.Companion.ARTICLE_KEY
import com.example.news.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.example.news.utils.Constants.Companion.SEARCH_NEWS_DELAY_TIME
import com.example.news.utils.Resource
import com.example.news.viewmodels.NewsViewModel
import com.example.news.viewmodels.NewsViewModelProviderFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment(), NewsAdapter.OnArticleClickListener {
    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    private var isLoading = false
    private var isLastPage = false
    private var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = ArticleDatabase.getInstance(requireContext())
        val newsRepository = NewsRepository(db.articleDao())
        val newsViewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
        viewModel = ViewModelProvider(this, newsViewModelProviderFactory)[NewsViewModel::class.java]

        setupRecyclerView()
        //to set a delay for sending req to server for search fro News
        var job: Job? = null
        binding.searchBar.addTextChangedListener {
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_DELAY_TIME)
                it?.let {
                    viewModel.searchNews(it.toString())
                }
                binding.emptyState.visibility = View.VISIBLE
            }
        }

        observeViews()
    }

    private fun setupRecyclerView() {
        binding.searchRv.apply {
            newsAdapter = NewsAdapter(requireContext(), this@SearchNewsFragment)
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }

    private fun observeViews() {
        viewModel.searchNewsLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles?.toList())
                        val totalPage = newsResponse.totalResults!! / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchNewsPageNumber == totalPage
                        if (isLastPage) {
                            binding.searchRv.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.data?.let {
                        response.message?.let {
                            Toast.makeText(
                                requireContext(),
                                "An Error occurred: $it",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }

        })
    }

    private fun hideProgressBar() {
        binding.searchProgressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.searchProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndLastPage = !isLoading && !isLastPage
            val isLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndLastPage && isLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.searchNews(binding.searchBar.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    override fun onClick(articles: Articles) {
        val bundle = Bundle().apply {
            putParcelable(ARTICLE_KEY, articles)
        }
        findNavController().navigate(
            R.id.action_navigation_search_to_articleFragment, bundle
        )
    }
}