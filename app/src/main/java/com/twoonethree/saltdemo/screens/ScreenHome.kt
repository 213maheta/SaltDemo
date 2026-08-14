package com.twoonethree.saltdemo.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

enum class BottomTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    News("News", Icons.Filled.Home, Icons.Outlined.Home),
    Bookmarks("Bookmarks", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder)
}

@Composable
fun ScreenHome(
    onArticleClick: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    // Tablets and foldables typically have screenWidthDp >= 600dp (Medium/Expanded)
    val isTablet = configuration.screenWidthDp >= 600

    var currentTab by rememberSaveable { mutableStateOf(BottomTab.News) }
    var selectedArticleUrl by rememberSaveable { mutableStateOf<String?>(null) }

    if (isTablet) {
        // ==========================================
        // TABLET TWO-PANE LAYOUT
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // Side Navigation Rail
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                BottomTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationRailItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) }
                    )
                }
            }

            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Left Pane: News List or Bookmarks List (40% width)
            Box(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight()
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "TabletListTransition"
                ) { tab ->
                    when (tab) {
                        BottomTab.News -> ScreenNewsList(
                            onArticleClick = { url ->
                                selectedArticleUrl = url
                            }
                        )
                        BottomTab.Bookmarks -> ScreenBookmarks(
                            onArticleClick = { url ->
                                selectedArticleUrl = url
                            }
                        )
                    }
                }
            }

            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Right Pane: Detail Content / WebView (58% width)
            Box(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
            ) {
                ArticleDetailPane(
                    articleUrl = selectedArticleUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        // ==========================================
        // PHONE SINGLE-PANE LAYOUT
        // ==========================================
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { tab ->
                when (tab) {
                    BottomTab.News -> ScreenNewsList(onArticleClick = onArticleClick)
                    BottomTab.Bookmarks -> ScreenBookmarks(onArticleClick = onArticleClick)
                }
            }
        }
    }
}