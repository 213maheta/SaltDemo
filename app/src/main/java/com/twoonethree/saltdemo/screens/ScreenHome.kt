package com.twoonethree.saltdemo.screens

import androidx.compose.runtime.Composable
import androidx.compose.animation.*
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
    var currentTab by rememberSaveable { mutableStateOf(BottomTab.News) }

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
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
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