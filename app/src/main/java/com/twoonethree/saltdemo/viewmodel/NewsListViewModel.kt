package com.twoonethree.saltdemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twoonethree.saltdemo.data.local.db.NewsCategory
import com.twoonethree.saltdemo.data.remote.network.NetworkResult
import com.twoonethree.saltdemo.data.repository.NewsRepository
import com.twoonethree.saltdemo.domain.model.Article
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NewsListUiState(
    val articles: List<Article> = emptyList(),
    val searchResults: List<Article> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isSearching: Boolean = false,
    val selectedCategory: NewsCategory = NewsCategory.GENERAL,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NewsListViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsListUiState())
    val uiState: StateFlow<NewsListUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private var currentPage = 1

    init {
        viewModelScope.launch {
            _uiState
                .map { it.selectedCategory }
                .distinctUntilChanged()
                .flatMapLatest { category ->
                    newsRepository.observeArticlesByCategory(category.apiValue)
                }
                .collect { articles ->
                    _uiState.update { it.copy(articles = articles) }
                }
        }

        viewModelScope.launch {
            searchQueryFlow
                .debounce(500L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                    } else {
                        performRemoteSearch(query)
                    }
                }
        }

        fetchHeadlines()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearchActive = query.isNotBlank()) }
        searchQueryFlow.value = query
    }

    fun onClearSearch() {
        _uiState.update { it.copy(searchQuery = "", isSearchActive = false, searchResults = emptyList()) }
        searchQueryFlow.value = ""
    }

    private suspend fun performRemoteSearch(query: String) {
        _uiState.update { it.copy(isSearching = true) }
        when (val result = newsRepository.searchArticlesRemote(query)) {
            is NetworkResult.Success -> {
                _uiState.update { it.copy(searchResults = result.data, isSearching = false) }
            }
            is NetworkResult.Failure -> {
                _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            }
        }
    }

    fun fetchHeadlines(loadMore: Boolean = false) {
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore || (loadMore && _uiState.value.endReached)) return

        viewModelScope.launch {
            if (loadMore) {
                _uiState.update { it.copy(isLoadingMore = true, error = null) }
                currentPage++
            } else {
                currentPage = 1
                _uiState.update { it.copy(isLoading = true, error = null, endReached = false) }
            }

            val category = _uiState.value.selectedCategory.apiValue
            when (val result = newsRepository.fetchTopHeadlines(category, currentPage)) {
                is NetworkResult.Success -> {
                    val count = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            endReached = count == 0
                        )
                    }
                }
                is NetworkResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = result.message ?: "Failed to fetch headlines"
                        )
                    }
                }
            }
        }
    }

    fun onCategorySelected(category: NewsCategory) {
        if (_uiState.value.selectedCategory != category) {
            _uiState.update { it.copy(selectedCategory = category) }
            fetchHeadlines()
        }
    }

    fun onBookmarkClick(article: Article) {
        viewModelScope.launch {
            newsRepository.toggleBookmark(article.url, !article.isBookmarked)
            if (_uiState.value.isSearchActive) {
                _uiState.update { state ->
                    state.copy(
                        searchResults = state.searchResults.map {
                            if (it.url == article.url) it.copy(isBookmarked = !article.isBookmarked) else it
                        }
                    )
                }
            }
        }
    }
}
