package com.example.mlallemant.mentalbattle.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class Analytics {

    companion object {
        const val EVENT_LOGIN = "event_login"
        const val FB_LOGIN = "fb_login"
        const val GOOGLE_LOGIN = "google_login"
        const val EMAIL_LOGIN = "email_login"
        const val GUEST_LOGIN = "guest_login"
    }

    fun logCustomEvent(context: Context, event: String, action: String, label: String? = null) {
        val analytics = FirebaseAnalytics.getInstance(context)
        val bundle = Bundle().apply {
            putString("action", action)
            label?.let { putString("label", label) }
        }
        analytics.logEvent(event, bundle)
    }
}