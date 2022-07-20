package com.fdl.storyapp.ui.login

import androidx.lifecycle.*
import com.fdl.storyapp.data.UserRepository
import com.fdl.storyapp.utilities.AuthError
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    var userModel = userRepository.userModel.asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.login(email, password)
            } catch (e: AuthError) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}