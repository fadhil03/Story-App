package com.fdl.storyapp.ui.home

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fdl.storyapp.data.StoryRepository
import com.fdl.storyapp.data.UserRepository
import com.fdl.storyapp.model.StoryModel
import com.fdl.storyapp.utilities.AuthError
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    val userModel = userRepository.userModel.asLiveData()

    val stories: LiveData<PagingData<StoryModel>> =
        userModel.switchMap {
            storyRepository.getStories(
                it.token ?: ""
            ).cachedIn(viewModelScope)
        }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.logout()
            } catch (e: AuthError) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}