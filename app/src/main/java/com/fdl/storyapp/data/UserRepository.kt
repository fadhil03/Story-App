package com.fdl.storyapp.data

import android.content.Context
import com.fdl.storyapp.R
import com.fdl.storyapp.data.remote.UserRemote
import com.fdl.storyapp.model.UserModel
import com.fdl.storyapp.utilities.AuthError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val context: Context,
    private val userRemoteDataSource: UserRemote,
    private val prefs: UserPreference
) {

    val userModel = prefs.getUser()

    suspend fun login(email: String, password: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = userRemoteDataSource.login(email, password)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()?.loginResult
                    result?.let {
                        val user = UserModel(
                            id = it.userId,
                            email = email,
                            name = it.name,
                            password = password,
                            token = it.token,
                            isLoggedIn = true
                        )
                        prefs.updateUser(user)
                    }
                } else {
                    throw AuthError(context.getString(R.string.login_failed))
                }
            } catch (e: Throwable) {
                throw AuthError(e.message.toString())
            }
        }
    }

    suspend fun register(name: String, email: String, password: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = userRemoteDataSource.register(name, email, password)
                if (!response.isSuccessful) {
                    throw AuthError(response.body()?.message ?: context.getString(R.string.regis_failed))
                }
            } catch (e: Throwable) {
                throw AuthError(e.message.toString())
            }
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            try {
                prefs.updateUser(UserModel(token = null, isLoggedIn = false))
            } catch (e: Throwable) {
                throw AuthError(e.message.toString())
            }
        }
    }
}