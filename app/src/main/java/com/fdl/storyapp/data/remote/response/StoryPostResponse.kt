package com.fdl.storyapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class StoryPostResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)
