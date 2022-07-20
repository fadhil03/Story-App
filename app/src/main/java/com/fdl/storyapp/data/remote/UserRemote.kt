package com.fdl.storyapp.data.remote

import com.fdl.storyapp.data.remote.api.UserApiServices

class UserRemote(private val userApi: UserApiServices) {

    suspend fun login(email: String, password: String) = userApi.login(email, password)

    suspend fun register(name: String, email: String, password: String) =
        userApi.register(name, email, password)
}