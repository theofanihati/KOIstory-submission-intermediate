package com.example.koistory.ui.view

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.koistory.R
import com.example.koistory.data.UserRepository
import com.example.koistory.data.pref.UserModel
import com.example.koistory.data.response.ErrorResponse
import com.example.koistory.data.response.ListStoryItem
import com.example.koistory.data.response.LoginResponse
import com.example.koistory.data.response.RegisterResponse
import com.example.koistory.data.response.Story
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import android.app.Application
import androidx.lifecycle.AndroidViewModel


class CombinedViewModel(application: Application, private val repository: UserRepository) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    val registerResponse: MutableLiveData<RegisterResponse> = MutableLiveData()

    private val _imageUri = MutableLiveData<Uri?>()
    val currentImageUri: LiveData<Uri?> = _imageUri

    private val _stories = MutableLiveData<List<ListStoryItem>>()
    val stories: LiveData<List<ListStoryItem>> = _stories

    private val _story = MutableLiveData<Story?>()
    val story: LiveData<Story?> = _story

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _sessionData = MutableLiveData<UserModel>()
    val sessionData: LiveData<UserModel> get() = _sessionData

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.register(name, email, password)
                registerResponse.value = response
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                _errorMessage.value = errorBody.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val response: LoginResponse = repository.loginUser(email, password)
            if (response != null) {
                _isLoading.value = false
                onResult(response.error == false)
            } else {
                _isLoading.value = false
                onResult(false)
            }
        }
    }

    fun logout(){
        _isLoading.value = true
        viewModelScope.launch {
            _isLoading.value = false
            repository.logout()
        }
    }

    fun getSession(){
        viewModelScope.launch {
            repository.getSession().collect { userModel ->
                _sessionData.value = userModel
            }
        }
    }

    fun getStories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getStories()
                _stories.value = response.listStory
            } catch (e: HttpException) {
                handleError(e.code())
            } catch (e: Exception) {
                _errorMessage.value = context.getString(R.string.tidak_ada_internet)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getStoryById(id: String){
        viewModelScope.launch {
            _isLoading.value = true
            try{
                val story = repository.getStoryById(id)
                if (story != null) {
                    _story.value = story
                } else {
                    _errorMessage.value = context.getString(R.string.tidak_ada_data)
                }
            } catch (e: HttpException){
                handleError(e.code())
            } catch (e: Exception) {
                _errorMessage.value =  context.getString(R.string.tidak_ada_internet)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadImage(file: File, description: String) = repository.uploadImage(file, description)

    fun setCurrentImageUri(uri: Uri?) {
        _imageUri.value = uri
    }
    private fun handleError(code: Int?) {
        _errorMessage.value = when (code) {
            400 ->  context.getString(R.string.error_bad_request)
            401 ->  context.getString(R.string.error_unauthorized)
            403 ->  context.getString(R.string.error_forbidden)
            404 ->  context.getString(R.string.error_not_found)
            500 ->  context.getString(R.string.error_internal)
            else ->  context.getString(R.string.error_unknown)
        }
    }
}

