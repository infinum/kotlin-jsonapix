package com.infinum.jsonapix.di

import android.content.Context
import co.infinum.retromock.Retromock
import com.infinum.jsonapix.TypeAdapterFactory
import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.retrofit.JsonXConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    fun retrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(JsonXConverterFactory(TypeAdapterFactory()))
            .baseUrl("https://www.example.com")
            .build()
    }

    @Provides
    fun retromock(retrofit: Retrofit, @ApplicationContext context: Context): Retromock {
        return Retromock.Builder()
            .retrofit(retrofit)
            .defaultBodyFactory(context.assets::open)
            .build()
    }

    @Provides
    fun service(retromock: Retromock): SampleApiService {
        return retromock.create(SampleApiService::class.java)
    }
}
