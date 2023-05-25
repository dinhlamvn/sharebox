package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dinhlam.sharebox.data.local.entity.VideoMixer

@Dao
interface VideoMixerDao {

    @Query("SELECT * FROM video_mixer WHERE share_id = :shareId")
    suspend fun find(shareId: String): VideoMixer?

    @Upsert
    suspend fun upsert(videoMixer: VideoMixer)

    @Query("SELECT * from video_mixer ORDER BY id DESC")
    suspend fun find(): List<VideoMixer>

    @Query("SELECT * from video_mixer ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun find(limit: Int, offset: Int): List<VideoMixer>
}