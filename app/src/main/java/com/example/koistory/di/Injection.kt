package com.example.koistory.di

import android.content.Context
import com.example.koistory.data.StoryRepository
import com.example.koistory.data.UserRepository
import com.example.koistory.data.local.database.StoryDatabase
import com.example.koistory.data.pref.UserPreference
import com.example.koistory.data.pref.dataStore
import com.example.koistory.data.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(apiService, userPreference)
    }
    fun provideStoryRepository(context: Context): StoryRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        val storyDatabase = StoryDatabase.getDatabase(context)
        return StoryRepository.getInstance(apiService, userPreference, storyDatabase)
    }
}
