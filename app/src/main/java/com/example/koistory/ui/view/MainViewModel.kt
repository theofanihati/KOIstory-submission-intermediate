package com.example.koistory.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.koistory.data.StoryRepository
import com.example.koistory.data.UserRepository
import com.example.koistory.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository, private val storyRepository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun logout(){
        _isLoading.value = true
        viewModelScope.launch {
            _isLoading.value = false
            repository.logout()
        }
    }

    val pagedStory: LiveData<PagingData<ListStoryItem>> = liveData {
        val stories = storyRepository.getPagedStories().cachedIn(viewModelScope)
        emitSource(stories)
    }
}