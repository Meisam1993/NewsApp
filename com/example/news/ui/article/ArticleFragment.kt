package com.example.news.ui.article

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.news.R
import com.example.news.databinding.FragmentArticleBinding
import com.example.news.db.ArticleDatabase
import com.example.news.model.Articles
import com.example.news.repository.NewsRepository
import com.example.news.utils.Constants
import com.example.news.viewmodels.NewsViewModel
import com.example.news.viewmodels.NewsViewModelProviderFactory
import com.google.android.material.snackbar.Snackbar

class ArticleFragment : Fragment() {
    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: NewsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = com.example.news.databinding.FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = ArticleDatabase.getInstance(requireContext())
        val newsRepository = NewsRepository(db.articleDao())
        val newsViewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
        viewModel = ViewModelProvider(this, newsViewModelProviderFactory)[NewsViewModel::class.java]

        //to get article which is clicked in other fragments here
        val article = arguments?.let {
            it.getParcelable<Articles>(Constants.ARTICLE_KEY)
        }
        binding.webView.apply {
            webViewClient = WebViewClient()
            article?.url?.let {
                loadUrl(article.url)
            }
        }

        binding.saveFab.setOnClickListener {
            binding.saveFab.setIconResource(R.drawable.baseline_favorite_24)
            article?.let { it1 -> viewModel.saveArticle(it1) }
            Snackbar.make(
                view,
                "The article is successfully saved",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}