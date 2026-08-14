package com.twoonethree.saltdemo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.twoonethree.saltdemo.data.local.db.NewsCategory
import com.twoonethree.saltdemo.domain.model.Article
import com.twoonethree.saltdemo.viewmodel.NewsListUiState
import com.twoonethree.saltdemo.viewmodel.NewsListViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenNewsList(
    onArticleClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewsListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var isSearchExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (totalItems > 0 && lastVisibleIndex >= totalItems - 3 && !uiState.isLoadingMore) {
                    viewModel.fetchHeadlines(loadMore = true)
                }
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        NewsTopAppBar(
            isSearchExpanded = isSearchExpanded,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChanged,
            onOpenSearch = { isSearchExpanded = true },
            onCloseSearch = {
                isSearchExpanded = false
                viewModel.onClearSearch()
            }
        )

        AnimatedVisibility(
            visible = !uiState.isSearchActive && !isSearchExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CategoryChipsRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.isSearchActive || (isSearchExpanded && uiState.searchQuery.isNotEmpty())) {
                SearchResultsContent(
                    results = uiState.searchResults,
                    query = uiState.searchQuery,
                    onArticleClick = onArticleClick,
                    onBookmarkClick = viewModel::onBookmarkClick
                )
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading && uiState.articles.isNotEmpty(),
                    onRefresh = { viewModel.fetchHeadlines() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    NewsFeedContent(
                        uiState = uiState,
                        listState = listState,
                        onArticleClick = onArticleClick,
                        onBookmarkClick = viewModel::onBookmarkClick,
                        onRetry = { viewModel.fetchHeadlines() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsTopAppBar(
    isSearchExpanded: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            focusRequester.requestFocus()
        }
    }

    TopAppBar(
        windowInsets = WindowInsets(0.dp),
        title = {
            if (isSearchExpanded) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search articles…") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear text")
                            }
                        }
                    }
                )
            } else {
                Text(
                    text = "Discover",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        navigationIcon = {
            if (isSearchExpanded) {
                IconButton(onClick = onCloseSearch) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close search"
                    )
                }
            }
        },
        actions = {
            if (!isSearchExpanded) {
                IconButton(onClick = onOpenSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Open search"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun NewsFeedContent(
    uiState: NewsListUiState,
    listState: LazyListState,
    onArticleClick: (String) -> Unit,
    onBookmarkClick: (Article) -> Unit,
    onRetry: () -> Unit
) {
    when {
        uiState.isLoading && uiState.articles.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null && uiState.articles.isEmpty() -> {
            ErrorState(message = uiState.error ?: "Something went wrong", onRetry = onRetry)
        }
        uiState.articles.isEmpty() -> {
            EmptyState()
        }
        else -> {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.articles, key = { it.url }) { article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(article.url) },
                        onBookmarkClick = { onBookmarkClick(article) }
                    )
                }
                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
                if (uiState.endReached && uiState.articles.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No more articles",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = article.urlToImage,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(Icons.Default.BrokenImage),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )

                article.description?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${article.sourceName} • ${article.publishedAt.take(10)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (article.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    selectedCategory: NewsCategory,
    onCategorySelected: (NewsCategory) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(NewsCategory.entries) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName, style = MaterialTheme.typography.labelLarge) }
            )
        }
    }
}

@Composable
private fun SearchResultsContent(
    results: List<Article>,
    query: String,
    onArticleClick: (String) -> Unit,
    onBookmarkClick: (Article) -> Unit
) {
    if (results.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No results for \"$query\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results, key = { it.url }) { article ->
                ArticleCard(
                    article = article,
                    onClick = { onArticleClick(article.url) },
                    onBookmarkClick = { onBookmarkClick(article) }
                )
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No articles found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
