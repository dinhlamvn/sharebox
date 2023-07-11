package com.dinhlam.sharebox.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.dinhlam.sharebox.data.local.entity.VideoMixer

@Dao
interface VideoMixerDao {

    @Query("SELECT * FROM video_mixer WHERE share_id = :shareId")
    suspend fun find(shareId: String): VideoMixer?

    @Upsert
    suspend fun upsert(videoMixer: VideoMixer)

    @Delete
    suspend fun delete(videoMixer: VideoMixer)

    @Query("SELECT * from video_mixer ORDER BY id DESC")
    suspend fun find(): List<VideoMixer>

    @Query(
        """
        SELECT vm.* 
        FROM video_mixer as vm 
        INNER JOIN share as s ON s.share_id = vm.share_id 
        LEFT JOIN box as b ON b.box_id = s.share_box_id 
        WHERE (s.share_box_id IS NULL) OR (s.share_box_id IS NOT NULL AND b.passcode IS NULL)
        ORDER BY s.share_date DESC 
        LIMIT :limit 
        OFFSET :offset
    """
    )
    suspend fun find(limit: Int, offset: Int): List<VideoMixer>

    @Query(
        """
        SELECT vm.* 
        FROM video_mixer as vm 
        INNER JOIN share as s ON s.share_id = vm.share_id
        WHERE s.share_box_id = :boxId 
        ORDER BY s.share_date DESC 
        LIMIT :limit 
        OFFSET :offset"""
    )
    suspend fun findWhereInBox(boxId: String, limit: Int, offset: Int): List<VideoMixer>

    @Query(
        """
        SELECT vm.* 
        FROM video_mixer as vm
        INNER JOIN share as s ON s.share_id = vm.share_id
        WHERE s.share_date < :timeToClean
        LIMIT 10
        """
    )
    suspend fun findVideoToCleanUp(timeToClean: Long): List<VideoMixer>
}