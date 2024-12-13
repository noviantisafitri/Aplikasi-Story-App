package com.dicoding.noviantisafitri.storyapp.data.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.noviantisafitri.storyapp.data.di.Event
import com.dicoding.noviantisafitri.storyapp.data.preference.SessionModel
import com.dicoding.noviantisafitri.storyapp.data.preference.SessionPreferences
import com.dicoding.noviantisafitri.storyapp.data.retrofit.ApiService
import com.dicoding.noviantisafitri.storyapp.responses.AddStoryResponse
import com.dicoding.noviantisafitri.storyapp.responses.ListStoryItem
import com.dicoding.noviantisafitri.storyapp.responses.LoginResponse
import com.dicoding.noviantisafitri.storyapp.responses.RegisterResponse
import com.dicoding.noviantisafitri.storyapp.ui.main.StorySource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Repository private constructor(
    private val pref: SessionPreferences,
    private val apiService: ApiService
) {

    private val _registerResponse = MutableLiveData<RegisterResponse>()
    val registerResponse: LiveData<RegisterResponse> = _registerResponse

    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    private val _uploadResponse = MutableLiveData<AddStoryResponse>()
    val uploadResponse: LiveData<AddStoryResponse> = _uploadResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastText = MutableLiveData<Event<String>>()
    val toastText: LiveData<Event<String>> = _toastText

    fun postRegister(name: String, email: String, password: String) {
        _isLoading.value = true
        val client = apiService.postRegister(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _registerResponse.value = response.body()
                    _toastText.value = Event(response.body()?.message ?: "Registration successful")
                } else {
                    val errorMessage = response.body()?.message ?: response.message()
                    _toastText.value = Event(errorMessage)
                    Log.e(TAG, "onFailure: ${response.code()} - $errorMessage")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                _toastText.value = Event("Error: ${t.message}")
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun postLogin(email: String, password: String) {
        _isLoading.value = true
        val client = apiService.postLogin(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _loginResponse.value = response.body()
                    _toastText.value = Event(response.body()?.message.toString())
                } else {
                    _toastText.value = Event(response.message().toString())
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _toastText.value = Event(t.message.toString())
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    fun getStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { StorySource(pref, apiService) }
        ).liveData
    }

    fun uploadStory(token: String, file: MultipartBody.Part, description: RequestBody) {
        _isLoading.value = true
        val client = apiService.postStory(token, file, description)
        client.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(call: Call<AddStoryResponse>, response: Response<AddStoryResponse>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _uploadResponse.value = response.body()
                    _toastText.value = Event(response.body()?.message.toString())
                } else {
                    _toastText.value = Event(response.message().toString())
                    Log.e(TAG, "onFailure: ${response.message()}, ${response.body()?.message.toString()}")
                }
            }

            override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                Log.d("error upload", t.message.toString())
            }
        })
    }

    fun getSession(): LiveData<SessionModel> {
        return pref.getSession().asLiveData().also {
            it.value?.let { session ->
                if (session.isLogin.not()) {
                    _toastText.value = Event("You must be logged in to perform this action.")
                }
            }
        }
    }

    suspend fun saveSession(session: SessionModel) {
        pref.saveSession(session)
    }

    suspend fun login() {
        pref.login()
    }

    suspend fun logout() {
        pref.logout()
    }

    companion object {
        private const val TAG = "Repository"
        @Volatile
        private var instance: Repository? = null
        fun getInstance(preferences: SessionPreferences, apiService: ApiService): Repository =
            instance ?: synchronized(this) {
                instance ?: Repository(preferences, apiService)
            }.also { instance = it }
    }
}