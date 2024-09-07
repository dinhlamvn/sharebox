package com.dinhlam.sharebox.helper

import com.dinhlam.sharebox.data.repository.BoxRepository
import com.dinhlam.sharebox.data.repository.ShareRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferDataHelper @Inject constructor(
    private val shareRepository: ShareRepository,
    private val boxRepository: BoxRepository,
) {

    suspend fun transferData(fromUserId: String, toUserId: String) {
        shareRepository.transferData(fromUserId, toUserId)
        boxRepository.transferData(fromUserId, toUserId)
    }
}