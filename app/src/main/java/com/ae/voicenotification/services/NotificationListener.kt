package com.ae.voicenotification.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ae.voicenotification.utils.Classifier

class NotificationListener: NotificationListenerService() {
    /*
    * Notification Listener Service
    *
    * Listens to notifications as they arrive
    * It is controlled by the OS and the user does not have fine controls to start or stop it
    */

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val notification = sbn.notification

            // Ignore Group Summaries
            if (notification.flags == Notification.FLAG_GROUP_SUMMARY)
                return

            var text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)
            text?.let {
                if(Classifier.isConfidential(text.toString()))
                    text = "Received confidential message"
            }
        }
    }

    companion object {
        private const val TAG = "com.ae"
        const val EXTRA_TEXT = "NOTIFICATION_TEXT"
    }
}