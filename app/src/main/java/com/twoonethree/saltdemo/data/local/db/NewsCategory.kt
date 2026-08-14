package com.twoonethree.saltdemo.data.local.db

enum class NewsCategory(val apiValue: String, val displayName: String) {
    GENERAL("general", "General"),
    BUSINESS("business", "Business"),
    ENTERTAINMENT("entertainment", "Entertainment"),
    HEALTH("health", "Health"),
    SCIENCE("science", "Science"),
    SPORTS("sports", "Sports"),
    TECHNOLOGY("technology", "Technology")
}
