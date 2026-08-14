package com.twoonethree.saltdemo.di

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.twoonethree.saltdemo.BuildConfig
import com.twoonethree.saltdemo.data.local.db.AppDatabase
import com.twoonethree.saltdemo.data.remote.api.NewsApi
import com.twoonethree.saltdemo.data.remote.network.*
import com.twoonethree.saltdemo.data.repository.NewsRepository
import com.twoonethree.saltdemo.viewmodel.BookmarksViewModel
import com.twoonethree.saltdemo.viewmodel.NewsDetailViewModel
import com.twoonethree.saltdemo.viewmodel.NewsListViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val networkModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single { LoggingInterceptorProvider.create() }
    single { ApiKeyInterceptor(apiKey = BuildConfig.NEWS_API_KEY) }
    single { RetryInterceptor() }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<ApiKeyInterceptor>())
            .addInterceptor(get<RetryInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(get())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }
}

val roomModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "news_app_db"
        ).build()
    }

    single { get<AppDatabase>().articleDao() }
}

val genericModule = module {

    single<NewsApi> {
        get<Retrofit>().create(NewsApi::class.java)
    }

    single { NewsRepository(get(), get()) }

    viewModel { NewsListViewModel(get()) }

    viewModel { (savedStateHandle: SavedStateHandle) ->
        NewsDetailViewModel(savedStateHandle, get())
    }

    viewModel { BookmarksViewModel(get()) }
}
