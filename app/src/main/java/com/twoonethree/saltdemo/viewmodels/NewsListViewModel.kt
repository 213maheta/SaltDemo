package com.twoonethree.saltdemo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twoonethree.saltdemo.model.Article
import com.twoonethree.saltdemo.network.NetworkResult
import com.twoonethree.saltdemo.repository.NewsRepository
import com.twoonethree.saltdemo.room.NewsCategory
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}

data class NewsListUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedCategory: NewsCategory = NewsCategory.GENERAL,
    val currentPage: Int = 1,
    val endReached: Boolean = false,
    // Search
    val searchQuery: String = "",
    val searchResults: List<Article> = emptyList(),
    val isSearchActive: Boolean = false   // true whenever query is non-blank
)

class NewsListViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsListUiState())
    val uiState: StateFlow<NewsListUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // Drives the debounced search — separate from uiState so debounce doesn't
    // get re-triggered by unrelated state changes (e.g. pagination updates)
    private val searchQueryFlow = MutableStateFlow("")

    init {
        observeArticles()
        observeSearch()
        fetchHeadlines()
    }

    private fun observeArticles() {
        viewModelScope.launch {
            _uiState.map { it.selectedCategory }
                .distinctUntilChanged()
                .flatMapLatest { category ->
                    repository.observeArticlesByCategory(category.apiValue)
                }
                .collect { articles ->
                    _uiState.update { it.copy(articles = articles) }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    val trimmed = query.trim()
                    if (trimmed.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        repository.searchArticles(trimmed)
                    }
                }
                .collect { results ->
                    _uiState.update { it.copy(searchResults = results) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(searchQuery = query, isSearchActive = query.isNotBlank())
        }
        searchQueryFlow.value = query
    }

    fun onClearSearch() {
        _uiState.update {
            it.copy(searchQuery = "", searchResults = emptyList(), isSearchActive = false)
        }
        searchQueryFlow.value = ""
    }

    fun onCategorySelected(category: NewsCategory) {
        _uiState.update {
            it.copy(selectedCategory = category, currentPage = 1, endReached = false)
        }
        fetchHeadlines()
    }

    fun fetchHeadlines(loadMore: Boolean = false) {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore) return
        if (loadMore && state.endReached) return

        viewModelScope.launch {
            val page = if (loadMore) state.currentPage + 1 else 1

            _uiState.update {
                if (loadMore) it.copy(isLoadingMore = true, error = null)
                else it.copy(isLoading = true, error = null)
            }

            when (val result = repository.fetchTopHeadlines(
                category = state.selectedCategory.apiValue,
                page = page
            )) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            currentPage = page,
                            endReached = result.data == 0
                        )
                    }
                }
                is NetworkResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, isLoadingMore = false, error = result.error.message)
                    }
                    _uiEvent.emit(UiEvent.ShowToast(result.error.message))
                }
            }
        }
    }

    fun onBookmarkClick(article: Article) {
        viewModelScope.launch {
            repository.toggleBookmark(article.url, !article.isBookmarked)
        }
    }
}