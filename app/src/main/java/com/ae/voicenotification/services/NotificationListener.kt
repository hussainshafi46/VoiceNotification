package com.ae.voicenotification.services

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.preference.PreferenceManager
import com.ae.voicenotification.utils.Classifier
import java.text.SimpleDateFormat
import java.util.*

class NotificationListener : NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
        // Start reader service as soon as listener service starts
        applicationContext.also {
            if (PreferenceManager.getDefaultSharedPreferences(it).getBoolean("master", true))
                it.startService(Intent(it, NotificationReader::class.java))
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val notification = sbn.notification

            // Ignore Group Summaries
            if (notification.flags == Notification.FLAG_GROUP_SUMMARY)
                return

            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext)

            // Check Service should be running
            if (!sharedPreferences.getBoolean("master", true))
                return

            // Check for Smart Hour Settings
            val currentHrs =
                SimpleDateFormat("HH", Locale.getDefault()).format(System.currentTimeMillis())
                    .toInt()
            if (sharedPreferences.getBoolean("smart", true) && currentHrs >= 22 && currentHrs <= 6)
                return

            var text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)
            text?.let {

                // Classify confidential messages
                if (Classifier.isConfidential(text.toString()))
                    text = "Received confidential message"

                Intent(applicationContext, NotificationReader::class.java).also {
                    it.putExtra(EXTRA_TEXT, text.toString())
                    startService(it)
                }
            }
        }
    }

    // To stop speaking of long notifications like emails
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        NotificationReader.stopSpeaking()
    }

    companion object {
        const val EXTRA_TEXT = "NOTIFICATION_TEXT"
    }
}