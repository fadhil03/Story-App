package com.fdl.storyapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.fdl.storyapp.data.database.StoryDatabase
import com.fdl.storyapp.data.remote.UserRemote
import com.fdl.storyapp.data.remote.api.RetrofitClient
import com.fdl.storyapp.data.remote.StoryRemote

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val userApi = RetrofitClient.getUserApi()
        val userRemoteDataSource = UserRemote(userApi)
        return UserRepository(context, userRemoteDataSource, UserPreference.getInstance(context.dataStore))
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val storyApi = RetrofitClient.getStoryApi()
        val storyRemoteDataSource = StoryRemote(storyApi)
        return StoryRepository(storyRemoteDataSource, StoryDatabase.getDatabase(context))
    }
}