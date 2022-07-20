package com.fdl.storyapp.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fdl.storyapp.model.StoryModel

@Dao
interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(stories: List<StoryModel>)

    @Query("SELECT * FROM storymodel")
    fun getStories(): PagingSource<Int, StoryModel>

    @Query("DELETE FROM storymodel")
    suspend fun deleteAllStories()
}