package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.VideoMixerDao
import com.dinhlam.sharebox.data.local.entity.VideoMixer
import com.dinhlam.sharebox.data.mapper.VideoMixerToVideoMixerDetailMapper
import com.dinhlam.sharebox.data.model.VideoMixerDetail
import com.dinhlam.sharebox.data.model.VideoSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoMixerRepository @Inject constructor(
    private val videoMixerDao: VideoMixerDao,
    private val shareRepository: ShareRepository,
    private val mapper: VideoMixerToVideoMixerDetailMapper
) {
    suspend fun findOne(shareId: String): VideoMixerDetail? {
        return videoMixerDao.runCatching {
            find(shareId)?.let { videoMixer ->
                val shareDetail = shareRepository.findOne(shareId) ?: return@let null
                mapper.map(videoMixer, shareDetail)
            }
        }.getOrNull()
    }

    suspend fun insert(
        shareId: String,
        originUrl: String,
        source: VideoSource,
        sourceId: String,
    ): Boolean {
        return videoMixerDao.runCatching {
            val videoMixer =
                VideoMixer(
                    shareId = shareId,
                    originalUrl = originUrl,
                    source = source,
                    sourceId = sourceId,
                )
            insert(videoMixer)
            true
        }.getOrDefault(false)
    }

    suspend fun find(): List<VideoMixerDetail> {
        return videoMixerDao.runCatching {
            find().mapNotNull { videoMixer ->
                val shareDetail =
                    shareRepository.findOne(videoMixer.shareId) ?: return@mapNotNull null
                mapper.map(videoMixer, shareDetail)
            }
        }.getOrDefault(emptyList())
    }

    suspend fun find(limit: Int, offset: Int): List<VideoMixerDetail> {
        return videoMixerDao.runCatching {
            find(limit, offset).mapNotNull { videoMixer ->
                val shareDetail =
                    shareRepository.findOne(videoMixer.shareId) ?: return@mapNotNull null
                mapper.map(videoMixer, shareDetail)
            }
        }.getOrDefault(emptyList())
    }

    suspend fun findWhereInBox(boxId: String, limit: Int, offset: Int): List<VideoMixerDetail> {
        return videoMixerDao.runCatching {
            findWhereInBox(boxId, limit, offset).mapNotNull { videoMixer ->
                val shareDetail =
                    shareRepository.findOne(videoMixer.shareId) ?: return@mapNotNull null
                mapper.map(videoMixer, shareDetail)
            }
        }.getOrDefault(emptyList())
    }

    suspend fun findVideoToCleanUp(timeToClean: Long): List<VideoMixer> =
        videoMixerDao.runCatching {
            findVideoToCleanUp(timeToClean)
        }.getOrDefault(emptyList())

    suspend fun delete(videoMixer: VideoMixer): Boolean = videoMixerDao.runCatching {
        delete(videoMixer)
        true
    }.getOrDefault(false)

    suspend fun count(): Int = videoMixerDao.runCatching {
        count()
    }.getOrDefault(0)
}