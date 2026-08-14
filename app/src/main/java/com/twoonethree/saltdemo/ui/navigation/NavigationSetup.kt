package com.twoonethree.saltdemo.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.twoonethree.saltdemo.ui.screens.ScreenBookmarks
import com.twoonethree.saltdemo.ui.screens.ScreenHome
import com.twoonethree.saltdemo.ui.screens.ScreenNewsDetail
import com.twoonethree.saltdemo.ui.screens.ScreenNewsList

@Composable
fun NavigationSetup() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenNavRoute.Home,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(280)
            ) + fadeIn(animationSpec = tween(280))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(280)
            ) + fadeOut(animationSpec = tween(280))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(280)
            ) + fadeIn(animationSpec = tween(280))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(280)
            ) + fadeOut(animationSpec = tween(280))
        }
    ) {
        composable<ScreenNavRoute.Home> {
            ScreenHome(
                onArticleClick = { articleUrl ->
                    navController.safeNavigate(ScreenNavRoute.NewsDetail(articleUrl = articleUrl))
                }
            )
        }

        composable<ScreenNavRoute.NewsDetail>(
            deepLinks = listOf(
                navDeepLink<ScreenNavRoute.NewsDetail>(
                    basePath = "newsapp://article"
                ),
                navDeepLink<ScreenNavRoute.NewsDetail>(
                    basePath = "https://saltdemo.com/article"
                )
            )
        ) {
            ScreenNewsDetail(
                onBackClick = { navController.safePopBackStack() }
            )
        }

        composable<ScreenNavRoute.BookMarks> {
            ScreenBookmarks(
                onArticleClick = { articleUrl ->
                    navController.safeNavigate(ScreenNavRoute.NewsDetail(articleUrl = articleUrl))
                }
            )
        }

        composable<ScreenNavRoute.NewsList> {
            ScreenNewsList(
                onArticleClick = { articleUrl ->
                    navController.safeNavigate(ScreenNavRoute.NewsDetail(articleUrl = articleUrl))
                }
            )
        }
    }
}

fun NavController.safePopBackStack(): Boolean {
    val currentEntry = currentBackStackEntry ?: return false
    return if (currentEntry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    } else {
        false
    }
}

fun <T : Any> NavController.safeNavigate(
    route: T,
    builder: (NavOptionsBuilder.() -> Unit)? = null
) {
    val currentEntry = currentBackStackEntry
    if (currentEntry == null || currentEntry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        if (builder != null) {
            navigate(route, builder)
        } else {
            navigate(route)
        }
    }
}
