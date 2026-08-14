package com.twoonethree.saltdemo.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twoonethree.saltdemo.repository.NewsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewsDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: NewsRepository
) : ViewModel() {

    private val articleUrl: String = checkNotNull(savedStateHandle["articleUrl"])

    val article = repository.observeArticleByUrl(articleUrl)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleBookmark() {
        viewModelScope.launch {
            val current = article.value
            if (current != null) {
                repository.toggleBookmark(current.url, !current.isBookmarked)
            }
        }
    }
}