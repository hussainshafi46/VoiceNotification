package com.ae.voicenotification.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.ae.voicenotification.R
import com.ae.voicenotification.services.NotificationReader
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private lateinit var builder: NotificationCompat.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val test = findPreference<Preference>("testing")
        test?.apply {
            onPreferenceClickListener = this@SettingsFragment
        }

        builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notify_icon)
            .setContentTitle("Test Notification")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        /*
        * Android Docs:
        * create the notification channel before posting any notifications on Android 8.0 and higher,
        * you should execute this code as soon as your app starts.
        * It's safe to call this repeatedly because
        * creating an existing notification channel performs no operation
        */
        createNotificationChannel()
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
        if (!isNotificationListenerStarted())
            requestSettings()
        if (languagesMissing())
            downloadLanguageResources()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Test Channel"
            val channel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {
        builder.apply {
            if (PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("default_lang", "en_IN") == "en_IN"
            )
                setContentText(getString(R.string.en))
            else
                setContentText(getString(R.string.hi))
        }
        with(NotificationManagerCompat.from(requireContext())) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }

    }

    private fun isNotificationListenerStarted() =
        NotificationManagerCompat.getEnabledListenerPackages(requireContext())
            .contains(requireContext().packageName)

    private fun requestSettings() {
        AlertDialog.Builder(requireContext())
            .setTitle("Enable Notification Settings")
            .setMessage("Please enable notification settings on the following screen for the app to run properly")
            .setPositiveButton("Enable") { p0, p1 -> startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
            .setNegativeButton("Discard") { p0, p1 -> requireActivity().finishAndRemoveTask() }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun languagesMissing(): Boolean {
        val availableLocale = Locale.getAvailableLocales()
        var hasEN = false
        var hasHI = false
        for (locale in availableLocale) {
            val langCode = "${locale.language}_${locale.country}"
            if (langCode == "en_IN")
                hasEN = true
            else if (langCode == "hi_IN")
                hasHI = true
            if (hasHI && hasEN)
                return false
        }
        return true
    }

    private fun downloadLanguageResources() {
        AlertDialog.Builder(requireContext())
            .setTitle("Require Installation")
            .setMessage("Please download English(India) and Hindi(India) languages from the following screen")
            .setPositiveButton("Install") { p0, p1 ->
                val install = Intent()
                install.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(install)
            }
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let { sp ->
            if (key == "default_lang") {
                val langCode = sp.getString(key, "en_IN")
                NotificationReader.setLanguage(langCode!!)
            } else if (key == "master") {
                val status = sp.getBoolean(key, true)
                requireContext().also {
                    if (status)
                        it.startService(Intent(it, NotificationReader::class.java))
                    else
                        it.stopService(Intent(it, NotificationReader::class.java))
                }
            }
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        preference?.apply {
            sendNotification()
            return true
        }
        return false
    }

    companion object {
        private const val CHANNEL_ID = "123"
        private const val notificationId = 351
    }
}