package com.dinhlam.sharebox.repository

import com.dinhlam.sharebox.database.dao.VoteDao
import com.dinhlam.sharebox.database.entity.Vote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteRepository @Inject constructor(
    private val voteDao: VoteDao
) {

    fun vote(shareId: String, userId: String): Boolean {
        val vote = Vote(shareId = shareId, userId = userId)
        return voteDao.runCatching {
            voteDao.insert(vote)
            true
        }.getOrDefault(false)
    }

    fun countVote(shareId: String) = voteDao.runCatching {
        countVote(shareId)
    }.getOrDefault(0)
}