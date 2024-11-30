package com.example.koistory.di

import android.content.Context
import com.example.koistory.data.UserRepository
import com.example.koistory.data.pref.UserPreference
import com.example.koistory.data.pref.dataStore
import com.example.koistory.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { userPreference.getSession().first() }
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(apiService, userPreference)
    }
}