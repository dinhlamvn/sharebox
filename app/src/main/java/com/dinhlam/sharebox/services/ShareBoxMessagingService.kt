package com.dinhlam.sharebox.services

import com.dinhlam.sharebox.logger.Logger
import com.google.firebase.messaging.FirebaseMessagingService

class ShareBoxMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.debug("New firebase token: $token")
    }
}