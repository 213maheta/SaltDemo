package com.twoonethree.saltdemo.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twoonethree.saltdemo.model.Article
import com.twoonethree.saltdemo.repository.NewsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarksViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    val bookmarkedArticles = repository.observeBookmarkedArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onBookmarkClick(article: Article) {
        viewModelScope.launch {
            repository.toggleBookmark(article.url, !article.isBookmarked)
        }
    }
}