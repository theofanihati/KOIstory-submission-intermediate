package com.example.koistory.ui.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViewsService
import com.example.koistory.data.StoryRepository
import com.example.koistory.data.UserRepository
import com.example.koistory.data.local.database.StoryDatabase
import com.example.koistory.data.pref.UserPreference
import com.example.koistory.data.pref.dataStore
import com.example.koistory.data.retrofit.ApiConfig

class StackWidgetService() : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val userRepository = StoryRepository(
            ApiConfig.getApiService(),
            UserPreference.getInstance(this.applicationContext.dataStore),
            StoryDatabase.getDatabase(applicationContext)
        )
        return StackRemoteViewsFactory(this.applicationContext, userRepository)
    }
}