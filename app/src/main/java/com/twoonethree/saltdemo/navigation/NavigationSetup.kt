package com.twoonethree.saltdemo.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.twoonethree.saltdemo.screens.ScreenBookmarks
import com.twoonethree.saltdemo.screens.ScreenHome
import com.twoonethree.saltdemo.screens.ScreenNewsDetail
import com.twoonethree.saltdemo.screens.ScreenNewsList

@Composable
fun NavigationSetup()
{
    val navController = rememberNavController()

    val onBackClick = remember { { navController.popBackStack() } }

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
                    navController.navigate(ScreenNavRoute.NewsDetail(articleUrl = articleUrl))
                }
            )
        }

        composable<ScreenNavRoute.NewsDetail> {
            ScreenNewsDetail(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ScreenNavRoute.BookMarks> {
            ScreenBookmarks(
                onArticleClick = { articleUrl ->
                    navController.navigate(ScreenNavRoute.NewsDetail(articleUrl = articleUrl))
                }
            )
        }

        composable<ScreenNavRoute.NewsList> {
            ScreenNewsList(
                onArticleClick = { articleUrl ->
                    navController.navigate(ScreenNavRoute.NewsDetail(articleUrl = articleUrl))
                }
            )
        }

    }
}