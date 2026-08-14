package com.twoonethree.saltdemo.navigation

import kotlinx.serialization.Serializable

sealed class ScreenNavRoute() {

    @Serializable data object Home : ScreenNavRoute()

    @Serializable
    data class NewsDetail(val articleUrl: String) : ScreenNavRoute()

    @Serializable data object BookMarks : ScreenNavRoute()
}