package com.twoonethree.saltdemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twoonethree.saltdemo.data.repository.NewsRepository
import com.twoonethree.saltdemo.domain.model.Article
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
