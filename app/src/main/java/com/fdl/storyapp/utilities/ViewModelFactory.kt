package com.fdl.storyapp.utilities

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fdl.storyapp.data.StoryRepository
import com.fdl.storyapp.data.UserRepository
import com.fdl.storyapp.data.Injection
import com.fdl.storyapp.ui.home.HomeViewModel
import com.fdl.storyapp.ui.login.LoginViewModel
import com.fdl.storyapp.ui.maps.MapsViewModel
import com.fdl.storyapp.ui.poststory.PostStoryViewModel
import com.fdl.storyapp.ui.register.RegisterViewModel

class ViewModelFactory(private val userRepository: UserRepository, private val storyRepository: StoryRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(userRepository, storyRepository) as T
            }
            modelClass.isAssignableFrom(PostStoryViewModel::class.java) -> {
                PostStoryViewModel(userRepository, storyRepository) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(userRepository, storyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null
        fun getInstance(context: Context): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(Injection.provideUserRepository(context), Injection.provideStoryRepository(context))
            }.also { instance = it }
    }
}