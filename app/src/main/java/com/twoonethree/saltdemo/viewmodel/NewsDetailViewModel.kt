package com.twoonethree.saltdemo.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.twoonethree.saltdemo.data.repository.NewsRepository
import com.twoonethree.saltdemo.domain.model.Article
import com.twoonethree.saltdemo.ui.navigation.ScreenNavRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class NewsDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val rawUrl: String = try {
        savedStateHandle.toRoute<ScreenNavRoute.NewsDetail>().articleUrl
    } catch (_: Exception) {
        savedStateHandle.get<String>("articleUrl").orEmpty()
    }

    // Make this public (remove `private`) so the UI can use it as a direct fallback
    val decodedUrl: String = try {
        URLDecoder.decode(rawUrl, StandardCharsets.UTF_8.toString())
    } catch (_: Exception) {
        rawUrl
    }

    val article: StateFlow<Article?> = newsRepository
        .observeArticleByUrl(decodedUrl)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleBookmark() {
        val currentArticle = article.value
        val isCurrentlyBookmarked = currentArticle?.isBookmarked ?: false

        viewModelScope.launch {
            newsRepository.toggleBookmark(decodedUrl, !isCurrentlyBookmarked)
        }
    }
}