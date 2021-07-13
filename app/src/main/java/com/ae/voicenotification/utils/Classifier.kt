package com.ae.voicenotification.utils

class Classifier {
    companion object {
        // Can Improve By training Model and/or adding more conditions
        // (no sufficient training data as of now)
        private val CONFIDENTIAL_PATTERN = listOf("(otp)", "(one\\stime\\spassword)", "(key)",
            "(password)", "(passphrase)", "(secret\\skey)", "(confidential)", "(secret)", "(user\\sid)",
            "(userid)", "(clientid)", "(client\\sid)")

        fun isConfidential(text: String): Boolean {
            val regexPattern = Regex(CONFIDENTIAL_PATTERN.joinToString(separator = "|"))

            return regexPattern.containsMatchIn(text.lowercase())
        }
    }
}