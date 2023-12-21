package com.example.news.ui.breaking

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.adapters.NewsAdapter
import com.example.news.databinding.FragmentBreakingNewsBinding
import com.example.news.db.ArticleDatabase
import com.example.news.model.Articles
import com.example.news.repository.NewsRepository
import com.example.news.utils.Constants.Companion.ARTICLE_KEY
import com.example.news.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.example.news.utils.Resource
import com.example.news.viewmodels.NewsViewModel
import com.example.news.viewmodels.NewsViewModelProviderFactory

class BreakingNewsFragment : Fragment(), NewsAdapter.OnArticleClickListener {
    private var _binding: FragmentBreakingNewsBinding? = null
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
        _binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = ArticleDatabase.getInstance(requireContext())
        val newsRepository = NewsRepository(db.articleDao())
        val newsViewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
        viewModel = ViewModelProvider(this, newsViewModelProviderFactory)[NewsViewModel::class.java]

        observeViews()

    }

    private fun observeViews() {
        //setup recyclerView
        binding.breakingRv.apply {
            newsAdapter = NewsAdapter(requireContext(), this@BreakingNewsFragment)
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }

        viewModel.breakingNewsLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles?.toList())
                        val totalPages = newsResponse.totalResults!! / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPageNumber == totalPages
                        if (isLastPage) {
                            binding.breakingRv.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(
                            requireContext(),
                            "An Error occurred: $it",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun hideProgressBar() {
        binding.breakingProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.breakingProgressBar.visibility = View.VISIBLE
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

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getBreakingNews("us")
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
            R.id.action_breaking_news_to_articleFragment, bundle
        )
    }
}