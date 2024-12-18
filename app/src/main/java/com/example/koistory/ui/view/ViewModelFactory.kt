package com.example.koistory.ui.view

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.koistory.data.StoryRepository
import com.example.koistory.data.UserRepository
import com.example.koistory.di.Injection

class ViewModelFactory(
    private val application: Application,
    private val repository: UserRepository,
    private val storyRepository: StoryRepository,
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CombinedViewModel::class.java) -> {
                CombinedViewModel(application, repository, storyRepository) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(repository, storyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null
        @JvmStatic
        fun getInstance(application: Application, context: Context): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(application, Injection.provideRepository(context), Injection.provideStoryRepository(context))
            }.also {
                instance = it
            }
    }
}
