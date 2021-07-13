package com.ae.voicenotification.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class NotificationReader: Service() {
    /*
    * Custom service that will read the notifications
    */

    override fun onCreate() {
        super.onCreate()
        mTextToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS)
                Log.d(TAG, "NotificationReader: onCreate() -> TTS initialized ")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val message = intent.getStringExtra(NotificationListener.EXTRA_TEXT)
            mTextToSpeech.speak(message, TextToSpeech.QUEUE_ADD,
                null, message.hashCode().toString())
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mTextToSpeech.shutdown()
    }

    companion object {
        private const val TAG = "com.ae"
        private lateinit var mTextToSpeech: TextToSpeech
        fun setLanguage(langCode: String) {
            mTextToSpeech.language = Locale(langCode)
        }
    }
}