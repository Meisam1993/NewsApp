package com.example.news.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.news.R
import com.example.news.model.Articles

class NewsAdapter(val context: Context, val listener: OnArticleClickListener) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.ivArticleImage)
        private val source: TextView = itemView.findViewById(R.id.tvSource)
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val description: TextView = itemView.findViewById(R.id.tvDescription)
        private val publishedAt: TextView = itemView.findViewById(R.id.tvPublishedAt)
        fun bindNews(articles: Articles) {
            Glide.with(context)
                .load(articles.urlToImage)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(image)
            source.text = articles.source?.name
            title.text = articles.title
            description.text = articles.description
            publishedAt.text = articles.publishedAt
            itemView.setOnClickListener {
                listener.onClick(articles)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Articles>() {
        override fun areItemsTheSame(oldItem: Articles, newItem: Articles): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Articles, newItem: Articles): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_article_preview, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.bindNews(article)
    }

    interface OnArticleClickListener {
        fun onClick(articles: Articles)
    }
}