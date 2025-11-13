package com.comprartir.mobile.core.network.di

import com.comprartir.mobile.BuildConfig
import com.comprartir.mobile.core.data.datastore.AuthTokenRepository
import com.comprartir.mobile.core.network.ComprartirApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        authTokenRepository: AuthTokenRepository,
    ): Interceptor = AuthTokenInterceptor(authTokenRepository)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        val normalizedBaseUrl = BuildConfig.COMPRARTIR_API_BASE_URL.let { base ->
            if (base.endsWith("/")) base else "$base/"
        }
        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideComprartirApi(retrofit: Retrofit): ComprartirApi = retrofit.create()
}

private class AuthTokenInterceptor(
    private val authTokenRepository: AuthTokenRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val token = runBlocking { authTokenRepository.currentToken() }
        val requestWithAuth: Request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .addHeader(AUTHORIZATION_HEADER, "Bearer $token")
                .build()
        }
        val response = chain.proceed(requestWithAuth)
        if (response.code == 401) {
            // Clear token immediately on 401 Unauthorized
            // This prevents further requests with invalid token
            // User will be redirected to login by the navigation logic
            android.util.Log.w("AuthTokenInterceptor", "401 Unauthorized - clearing token")
            runBlocking { authTokenRepository.clearToken() }
        }
        return response
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
    }
}
