package com.fdl.storyapp.model

data class UserModel(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val token: String? = null,
    var isLoggedIn: Boolean? = null
)