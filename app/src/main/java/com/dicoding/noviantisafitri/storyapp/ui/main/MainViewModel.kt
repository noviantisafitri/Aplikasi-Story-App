package com.dicoding.noviantisafitri.storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.noviantisafitri.storyapp.responses.ListStoryItem
import com.dicoding.noviantisafitri.storyapp.data.database.Repository
import com.dicoding.noviantisafitri.storyapp.data.di.Event
import com.dicoding.noviantisafitri.storyapp.data.preference.SessionModel
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {
    val toastText: LiveData<Event<String>> = repository.toastText
    val getListStories: LiveData<PagingData<ListStoryItem>> =
        repository.getStories().cachedIn(viewModelScope)
    val isLoggedOut = MutableLiveData<Boolean>()


    fun getSession(): LiveData<SessionModel> {
        return repository.getSession()
    }

    fun logoutApp() {
        viewModelScope.launch {
            repository.logout()
            isLoggedOut.value = true
        }
    }
}