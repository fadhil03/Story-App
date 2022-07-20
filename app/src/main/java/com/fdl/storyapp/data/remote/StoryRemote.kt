package com.fdl.storyapp.data.remote

import com.fdl.storyapp.data.remote.api.StoryApi
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRemote(private val storyApi: StoryApi) {

    suspend fun getStories(token: String, page: Int, size: Int) =
        storyApi.getStories("Bearer $token", page, size)

    suspend fun getStoriesWithLocation(token: String, page: Int, size: Int) =
        storyApi.getStories("Bearer $token", page, size, 1)

    suspend fun postStories(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: Double? = null,
        lon: Double? = null
    ) = storyApi.postStory("Bearer $token", file, description, lat, lon)
}