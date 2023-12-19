package com.example.news.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.adapters.NewsAdapter
import com.example.news.databinding.FragmentSavedNewsBinding
import com.example.news.db.ArticleDatabase
import com.example.news.model.Articles
import com.example.news.repository.NewsRepository
import com.example.news.utils.Constants
import com.example.news.viewmodels.NewsViewModel
import com.example.news.viewmodels.NewsViewModelProviderFactory
import com.google.android.material.snackbar.Snackbar

class SavedNewsFragment : Fragment(), NewsAdapter.OnArticleClickListener {
    private var _binding: FragmentSavedNewsBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = ArticleDatabase.getInstance(requireContext())
        val newsRepository = NewsRepository(db.articleDao())
        val newsViewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
        viewModel = ViewModelProvider(this, newsViewModelProviderFactory)[NewsViewModel::class.java]

        setupRecyclerView()
        observeViews()
    }

    private fun observeViews() {
        //to get saved News
        viewModel.getAllSavedArticles().observe(viewLifecycleOwner, Observer {
            newsAdapter.differ.submitList(it)
        })

        //to delete article by swiping
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.differ.currentList[position]
                viewModel.deleteArticle(article)
                view?.let {
                    Snackbar.make(it, "Article successfully deleted ", Snackbar.LENGTH_SHORT)
                        .apply {
                            setAction("Undo") {
                                viewModel.saveArticle(article)
                            }
                                .show()
                        }
                }
            }

        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.savedRv)
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(requireContext(), this@SavedNewsFragment)
        binding.savedRv.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(articles: Articles) {
        val bundle = Bundle().apply {
            putParcelable(Constants.ARTICLE_KEY, articles)
        }
        findNavController().navigate(
            R.id.action_saved_news_to_articleFragment, bundle
        )
    }
}