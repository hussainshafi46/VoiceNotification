package com.ae.voicenotification.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.ae.voicenotification.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}