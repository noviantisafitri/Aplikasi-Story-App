package com.dicoding.noviantisafitri.storyapp.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dicoding.noviantisafitri.storyapp.data.database.Repository
import com.dicoding.noviantisafitri.storyapp.data.preference.SessionPreferences
import com.dicoding.noviantisafitri.storyapp.data.retrofit.ApiConfig


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("token")

object Injection {
    fun provideRepository(context: Context): Repository {
        val preferences = SessionPreferences.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(context)
        return Repository.getInstance(preferences, apiService)
    }
}