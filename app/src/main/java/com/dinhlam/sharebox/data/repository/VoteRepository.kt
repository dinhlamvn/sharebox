package com.dinhlam.sharebox.data.repository

import com.dinhlam.sharebox.data.local.dao.VoteDao
import com.dinhlam.sharebox.data.local.entity.Vote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteRepository @Inject constructor(
    private val voteDao: VoteDao
) {

    suspend fun vote(shareId: String, userId: String): Boolean {
        val vote = Vote(shareId = shareId, userId = userId)
        return voteDao.runCatching {
            voteDao.insert(vote)
            true
        }.getOrDefault(false)
    }

    suspend fun countVote(shareId: String) = voteDao.runCatching {
        countVote(shareId)
    }.getOrDefault(0)
}