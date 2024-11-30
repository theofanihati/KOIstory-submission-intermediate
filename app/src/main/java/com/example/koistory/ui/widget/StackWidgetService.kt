package com.example.koistory.ui.widget

import android.content.Intent
import android.util.Log
import android.widget.RemoteViewsService
import com.example.koistory.data.UserRepository
import com.example.koistory.data.pref.UserPreference
import com.example.koistory.data.pref.dataStore
import com.example.koistory.data.retrofit.ApiConfig

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val userRepository = UserRepository(
            ApiConfig.getApiService(),
            UserPreference.getInstance(this.applicationContext.dataStore)
        )
        Log.d("StackWidgetService", "CEKKKKKKKKKKK")
        return StackRemoteViewsFactory(this.applicationContext, userRepository)
    }
}