package com.example.koistory.data

import androidx.lifecycle.liveData
import com.example.koistory.data.pref.UserModel
import com.example.koistory.data.pref.UserPreference
import com.example.koistory.data.response.FileUploadResponse
import com.example.koistory.data.response.LoginResponse
import com.example.koistory.data.response.Story
import com.example.koistory.data.response.StoryResponse
import com.example.koistory.data.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    suspend fun register(name: String, email: String, password: String) = apiService.register(name, email, password)

    suspend fun login(email: String, password: String): LoginResponse? {
        val response = apiService.login(email, password)
        if (response.isSuccessful) {
            val loginResult = response.body()?.loginResult
            if (loginResult != null) {
                saveSession(
                    UserModel(
                    loginResult.name ?: "",
                    loginResult.userId ?: "",
                    loginResult.token ?: "",
                    true
                    )
                )
            }
            return response.body()
        }
        return null
    }

    suspend fun loginUser(email: String, password: String): LoginResponse {
        val userRepository = UserRepository(apiService, userPreference)
        val response = userRepository.login(email, password)

        if (response != null && response.error == false) {
            val loginResult = response.loginResult
            if (loginResult != null) {
                val userModel = UserModel(
                    name = loginResult.name ?: "",
                    userId = loginResult.userId ?: "",
                    token = loginResult.token ?: "",
                    isLogin = true,
                )
                val token = userModel.token
                userRepository.saveSession(userModel)
            }
            println("Login berhasil, sesi sudah disimpan")
        } else {
            println("Login gagal: ${response?.message}")
        }
        return response ?: LoginResponse(error = true, message = "Login gagal")
    }

    private suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun getStories(): StoryResponse {
        val user = userPreference.getSession().first()
        val token = "Bearer ${user.token}"
        try {
            return apiService.getStories(token)
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

    suspend fun logout() {
        userPreference.logout()
        val user = userPreference.getSession().firstOrNull()
        val token = user?.token
        println("Logout berhasil, sesi sudah dihapus")
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference)
            }.also { instance = it }
    }
}