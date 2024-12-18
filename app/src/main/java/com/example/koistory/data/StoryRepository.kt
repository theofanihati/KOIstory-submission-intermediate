package com.example.koistory.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.koistory.data.local.database.StoryDatabase
import com.example.koistory.data.pref.UserPreference
import com.example.koistory.data.response.FileUploadResponse
import com.example.koistory.data.response.ListStoryItem
import com.example.koistory.data.response.Story
import com.example.koistory.data.response.StoryResponse
import com.example.koistory.data.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class StoryRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference,
    private val storyDatabase: StoryDatabase
) {
    suspend fun getStories(): StoryResponse {
        val user = userPreference.getSession().first()
        val token = "Bearer ${user.token}"
        try {
            return apiService.getStories(token)
        } catch (e: HttpException){
            throw e
        }
    }

    suspend fun getStoriesWithLocation(): StoryResponse {
        val user = userPreference.getSession().first()
        val token = "Bearer ${user.token}"
        try {
            return apiService.getStoriesWithLocation(token)
        } catch (e: HttpException){
            throw e
        }
    }

    suspend fun getStoryById(id: String): Story? {
        val user = userPreference.getSession().first()
        val token = "Bearer ${user.token}"

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStoryById(id, token).execute()
                if (response.isSuccessful) {
                    val story = response.body()?.story
                    story
                } else {
                    null
                }
            } catch (e: HttpException){
                throw e
            }
        }
    }

    fun uploadImage(imageFile: File, description: String) = liveData {
        emit(ResultState.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        val user = userPreference.getSession().first()
        val token = "Bearer ${user.token}"

        try {
            val successResponse = apiService.uploadImage(token, multipartBody, requestBody)
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    suspend fun getPagedStories(): LiveData<PagingData<ListStoryItem>> {
        val user = userPreference.getSession().first()
        val token = "Bearer ${user.token}"
        try {
            @OptIn(ExperimentalPagingApi::class)
            return Pager(
                config = PagingConfig(
                    pageSize = 5,
                    maxSize = 15
                ),
                remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
                pagingSourceFactory = {
                    storyDatabase.storyDao().getAllQuote()
                }
            ).liveData
        } catch (e: HttpException){
            throw e
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference,
            storyDatabase: StoryDatabase
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference, storyDatabase)
            }.also { instance = it }
    }
}