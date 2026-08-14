package com.twoonethree.saltdemo.ui.screens

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.twoonethree.saltdemo.data.repository.NewsRepository
import com.twoonethree.saltdemo.domain.model.Article
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailPane(
    articleUrl: String?,
    modifier: Modifier = Modifier,
    newsRepository: NewsRepository = koinInject()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val article by produceState<Article?>(initialValue = null, key1 = articleUrl) {
        if (articleUrl.isNullOrBlank()) {
            value = null
        } else {
            newsRepository.observeArticleByUrl(articleUrl).collect {
                value = it ?: Article(
                    title = "Article Preview",
                    description = null,
                    content = null,
                    category = "general",
                    url = articleUrl,
                    urlToImage = null,
                    publishedAt = "",
                    sourceName = "Web",
                    isBookmarked = false
                )
            }
        }
    }

    if (articleUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select an article to view details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0.dp),
                    title = {
                        Text(
                            text = article?.sourceName ?: "Article",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    },
                    actions = {
                        article?.let { currentArticle ->
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    newsRepository.toggleBookmark(
                                        currentArticle.url,
                                        !currentArticle.isBookmarked
                                    )
                                }
                            }) {
                                Icon(
                                    imageVector = if (currentArticle.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (currentArticle.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${currentArticle.title}\n${currentArticle.url}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share article"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }

                            IconButton(onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentArticle.url))
                                context.startActivity(browserIntent)
                            }) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in browser")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            var isLoading by remember(articleUrl) { mutableStateOf(true) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setBackgroundColor(AndroidColor.TRANSPARENT)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                }
                            }
                        }
                    },
                    update = { webView ->
                        if (webView.url != articleUrl) {
                            isLoading = true
                            webView.loadUrl(articleUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
