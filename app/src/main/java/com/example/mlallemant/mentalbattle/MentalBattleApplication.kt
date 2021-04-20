package com.example.mlallemant.mentalbattle

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class MentalBattleApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebaseRemoteConfig()
    }

    private fun initFirebaseRemoteConfig() {
        FirebaseApp.initializeApp(this)
        FirebaseRemoteConfig.getInstance().apply {
            //set this during development
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
            setConfigSettingsAsync(configSettings)
            //set this during development
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate().addOnCompleteListener { task ->
                val updated = task.result
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d("TAG", "Remote config Config params updated: $updated")
                } else {
                    Log.d("TAG", "Remote config Config params updated: $updated")
                }
            }
        }
    }

    companion object {
        const val REMOTE_CONFIG_TEST_VALUE = "test_value"
        const val REMOTE_CONFIG_SIGNIN_WITH_GOOGLE = "signin_with_google"
        const val REMOTE_CONFIG_SIGNIN_WITH_FACEBOOK = "signin_with_facebook"
    }
}