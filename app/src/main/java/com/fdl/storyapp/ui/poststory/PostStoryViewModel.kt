package com.fdl.storyapp.ui.poststory

import androidx.lifecycle.*
import com.fdl.storyapp.data.StoryRepository
import com.fdl.storyapp.data.UserRepository
import com.fdl.storyapp.utilities.StoryError
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PostStoryViewModel(userRepository: UserRepository, private val storyRepository: StoryRepository) : ViewModel() {

    val userModel = userRepository.userModel.asLiveData()

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun uploadImage(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        latLng: LatLng? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                storyRepository.postStory(token, file, description, latLng)
                _isSuccess.value = true
            } catch (e: StoryError) {
                _errorMessage.value = e.message
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}