package com.dicoding.noviantisafitri.storyapp.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.noviantisafitri.storyapp.responses.RegisterResponse
import com.dicoding.noviantisafitri.storyapp.data.database.Repository
import com.dicoding.noviantisafitri.storyapp.data.di.Event
import kotlinx.coroutines.launch

class RegisterViewModel (private val repository: Repository) : ViewModel() {
    val registerResponse: LiveData<RegisterResponse> = repository.registerResponse
    val toastText: LiveData<Event<String>> = repository.toastText
    val isLoading: LiveData<Boolean> = repository.isLoading

    fun dataRegister(name: String, email: String, password: String) {
        viewModelScope.launch {
            repository.postRegister(name, email, password)
        }
    }
}