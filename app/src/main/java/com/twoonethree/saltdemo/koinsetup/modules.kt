package com.twoonethree.saltdemo.koinsetup

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.twoonethree.saltdemo.BuildConfig
import com.twoonethree.saltdemo.api.NewsApi
import com.twoonethree.saltdemo.network.ApiKeyInterceptor
import com.twoonethree.saltdemo.network.LoggingInterceptorProvider
import com.twoonethree.saltdemo.network.NetworkConstants
import com.twoonethree.saltdemo.network.RetryInterceptor
import com.twoonethree.saltdemo.repository.NewsRepository
import com.twoonethree.saltdemo.room.AppDatabase
import com.twoonethree.saltdemo.viewmodels.BookmarksViewModel
import com.twoonethree.saltdemo.viewmodels.NewsDetailViewModel
import com.twoonethree.saltdemo.viewmodels.NewsListViewModel
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

    // Interceptors
    single { LoggingInterceptorProvider.create() }
    single { ApiKeyInterceptor(apiKey = BuildConfig.NEWS_API_KEY) }
    single { RetryInterceptor() } // <-- Added missing registration

    // OkHttp client
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<ApiKeyInterceptor>())
            .addInterceptor(get<RetryInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit instance
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