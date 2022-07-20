package com.fdl.storyapp.ui.maps

import androidx.lifecycle.*
import com.fdl.storyapp.data.StoryRepository
import com.fdl.storyapp.data.UserRepository
import com.fdl.storyapp.model.StoryModel
import com.fdl.storyapp.utilities.StoryError
import kotlinx.coroutines.launch

class MapsViewModel(userRepository: UserRepository, private val storyRepository: StoryRepository) :
    ViewModel() {

    val userModel = userRepository.userModel.asLiveData()

    private val _stories = MutableLiveData<List<StoryModel>>()
    val stories: LiveData<List<StoryModel>> = _stories

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getStoriesWithLocation(token: String) {
        viewModelScope.launch {
            try {
                val stories = storyRepository.getStoriesWithLocation(token = token)
                _stories.value = stories
            } catch (e: StoryError) {
                _errorMessage.value = e.message.toString()
            }
        }
    }
}