package com.dicoding.noviantisafitri.storyapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.noviantisafitri.storyapp.data.database.Repository
import com.dicoding.noviantisafitri.storyapp.data.di.Event
import com.dicoding.noviantisafitri.storyapp.data.preference.SessionModel
import com.dicoding.noviantisafitri.storyapp.responses.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: Repository) : ViewModel() {

    val loginResponse: LiveData<LoginResponse> = repository.loginResponse
    val toastText: LiveData<Event<String>> = repository.toastText

    fun postLogin(email: String, password: String) {
        viewModelScope.launch {
            repository.postLogin(email, password)
        }
    }

    fun saveSession(session: SessionModel) {
        viewModelScope.launch {
            repository.saveSession(session)
        }
    }
}
